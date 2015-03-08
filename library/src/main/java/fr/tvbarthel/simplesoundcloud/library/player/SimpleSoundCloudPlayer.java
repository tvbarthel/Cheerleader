package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.remote.RemoteControlClientCompat;
import fr.tvbarthel.simplesoundcloud.library.remote.RemoteControlHelper;

/**
 * Service used as SoundCloudPlayer.
 */
public class SimpleSoundCloudPlayer extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    /**
     * Action used for toggle playback event
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_TOGGLE_PLAYBACK = "sound_cloud_toggle_playback";

    /**
     * Action used to skip to the next track of the sound cloud player.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_NEXT_TRACK = "sound_cloud_player_next";

    /**
     * Action used to skip to the previous track of the sound cloud player.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_PREVIOUS_TRACK = "sound_cloud_player_previous";

    /**
     * Action used to skip to clear the notification.
     * <p/>
     * package private, used by the SimpleSoundCloudNotificationManager for PendingIntent.
     */
    static final String ACTION_CLEAR_NOTIFICATION = "sound_cloud_player_clear";

    /**
     * Action used to stop the player when audio signal has becoming noisy by the system.
     * http://developer.android.com/reference/android/media/AudioManager.html#ACTION_AUDIO_BECOMING_NOISY
     */
    static final String ACTION_AUDIO_BECOMING_NOISY = "sound_cloud_player_becoming_noisy";

    /**
     * Action used to play a track.
     */
    private static final String ACTION_PLAY = "sound_cloud_play";

    /**
     * Action used to resume the sound cloud player.
     */
    private static final String ACTION_PAUSE_PLAYER = "sound_cloud_player_resume";

    /**
     * Action used to pause the sound cloud player.
     */
    private static final String ACTION_RESUME_PLAYER = "sound_cloud_player_pause";

    /**
     * Action used to stop the sound cloud player.
     */
    private static final String ACTION_STOP_PLAYER = "sound_cloud_player_stop";

    /**
     * Action used to change the cursor of the current track.
     */
    private static final String ACTION_SEEK_TO = "sound_cloud_player_seek_to";

    /**
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Bundle key used to pass track url.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK = "sound_cloud_player_bundle_key_track_url";

    /**
     * Bundle key used to seek to a given position.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION = "sound_cloud_player_bundle_key_seek_to";

    /**
     * what id used to identify "play" message.
     */
    private static final int WHAT_PLAY = 0;

    /**
     * what id used to identify "pause player" message.
     */
    private static final int WHAT_PAUSE_PLAYER = 1;

    /**
     * what id used to identify "resume player" message.
     */
    private static final int WHAT_RESUME_PLAYER = 2;

    /**
     * what id used to identify "next track" message.
     */
    private static final int WHAT_NEXT_TRACK = 3;

    /**
     * what id used to identify "previous track" message.
     */
    private static final int WHAT_PREVIOUS_TRACK = 4;

    /**
     * what id used to identify "seek to" message.
     */
    private static final int WHAT_SEEK_TO = 5;

    /**
     * what id used to stop playback request
     */
    private static final int WHAT_STOP_PLAYER = 6;

    /**
     * what id used to stop the service.
     */
    private static final int WHAT_RELEASE_PLAYER = 7;

    /**
     * what id used to clear the player.
     */
    private static final int WHAT_CLEAR_PLAYER = 9;

    /**
     * Log cat and thread name prefix.
     */
    private static final String TAG = SimpleSoundCloudPlayer.class.getSimpleName();

    /**
     * Delay used to avoid useless action in case of spam action.
     */
    private static final int MESSAGE_DELAY_MILLI = 100;

    /**
     * Max idle period after which the service will be stopped.
     */
    private static final int IDLE_PERIOD_MILLI = 10000;

    /**
     * Path param used to access streaming url.
     */
    private static final String SOUND_CLOUD_CLIENT_ID_PARAM = "?client_id=";

    /**
     * Tag used in debugging message for wifi lock.
     */
    private static final String WIFI_LOCK_TAG = TAG + "wifi_lock";

    /**
     * Name for the internal handler thread.
     */
    private static final String THREAD_NAME = TAG + "player_thread";

    /**
     * Handler used to execute works on an {@link android.os.HandlerThread}
     */
    private Handler mPlayerHandler;

    /**
     * Handler used to stop the service when idle period ends.
     */
    private Handler mStopServiceHandler;

    /**
     * MediaPlayer used to play music.
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Used to know if the player is paused.
     */
    private boolean mIsPaused;

    /**
     * Used to know if the player has leary played a track.
     */
    private boolean mHasAlreadyPlayed;

    /**
     * Lock used to keep wifi while playing.
     */
    private WifiManager.WifiLock mWifiLock;

    /**
     * SoundCloudClientId.
     */
    private String mSoundCloundClientId;

    /**
     * Used to broadcast events.
     */
    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Sound cloud notification manager.
     */
    private SimpleSoundCloudNotificationManager mSimpleSoundCloudNotificationManager;

    /**
     * Used to managed the internal playlist.
     */
    private SimpleSoundCloudPlayerPlaylist mSimpleSoundCloudPlayerPlaylist;

    /**
     * System service used to managed audio through user device.
     */
    private AudioManager mAudioManager;

    private ComponentName mMediaButtonReceiverComponent;

    private RemoteControlClientCompat mRemoteControlClientCompat;

    /**
     * Start the playback.
     * <p/>
     * Play the track matching the given position in the given playlist.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     * @param track    the track which will be played.
     */
    public static void play(Context context, String clientId, SoundCloudTrack track) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK, track);
        context.startService(intent);
    }

    /**
     * Pause the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void pause(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PAUSE_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Resume the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void resume(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_RESUME_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Stop the SoundCloud player.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void stop(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_STOP_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Seek to the precise track position.
     * <p/>
     * The current playing state of the SoundCloud player will be kept.
     * <p/>
     * If playing it remains playing, if paused it remains paused.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     * @param milli    time in milli of the position.
     */
    public static void seekTo(Context context, String clientId, int milli) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_SEEK_TO);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION, milli);
        context.startService(intent);
    }

    /**
     * Register a listener to catch player event.
     *
     * @param context  context used to register the listener.
     * @param listener listener to register.
     */
    public static void registerListener(Context context, SimpleSoundCloudListener listener) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_PLAYER_PAUSED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_SEEK_COMPLETE);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_PLAYER_DESTROYED);

        LocalBroadcastManager.getInstance(context.getApplicationContext())
                .registerReceiver(listener, filter);
    }

    /**
     * Unregister a registered listener.
     *
     * @param context  context used to unregister the listener.
     * @param listener listener to unregister.
     */
    public static void unregisterListener(Context context, SimpleSoundCloudListener listener) {
        LocalBroadcastManager.getInstance(context.getApplicationContext())
                .unregisterReceiver(listener);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_AUDIO);
        thread.start();

        mPlayerHandler = new PlayerHandler(thread.getLooper());
        mMediaPlayer = new MediaPlayer();

        initializeMediaPlayer();
        initializeStopHandler(thread.getLooper());

        mWifiLock = ((WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        mSimpleSoundCloudNotificationManager = SimpleSoundCloudNotificationManager.getInstance(this);

        mSimpleSoundCloudPlayerPlaylist = SimpleSoundCloudPlayerPlaylist.getInstance();

        mHasAlreadyPlayed = false;

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        initLockScreenRemoteControlClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);
        mAudioManager.abandonAudioFocus(null);

        mPlayerHandler.removeCallbacksAndMessages(null);
        stopForeground(true);

        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_PLAYER_DESTROYED);
        mLocalBroadcastManager.sendBroadcast(intent);

        mMediaPlayer.release();
        mMediaPlayer = null;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message message = mPlayerHandler.obtainMessage();
            Bundle extra = intent.getExtras();

            if (extra != null) {
                // client id should be passed for each command as service could have been restarted
                // by the system.
                mSoundCloundClientId = extra.getString(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID);

                // transfer args to the handler.
                message.setData(extra);
            }

            switch (intent.getAction()) {
                case ACTION_PLAY:
                    message.what = WHAT_PLAY;
                    break;
                case ACTION_PAUSE_PLAYER:
                    message.what = WHAT_PAUSE_PLAYER;
                    break;
                case ACTION_RESUME_PLAYER:
                    message.what = WHAT_RESUME_PLAYER;
                    break;
                case ACTION_STOP_PLAYER:
                    message.what = WHAT_STOP_PLAYER;
                    break;
                case ACTION_NEXT_TRACK:
                    message.what = WHAT_NEXT_TRACK;
                    break;
                case ACTION_PREVIOUS_TRACK:
                    message.what = WHAT_PREVIOUS_TRACK;
                    break;
                case ACTION_SEEK_TO:
                    message.what = WHAT_SEEK_TO;
                    break;
                case ACTION_TOGGLE_PLAYBACK:
                    if (mIsPaused) {
                        message.what = WHAT_RESUME_PLAYER;
                    } else {
                        message.what = WHAT_PAUSE_PLAYER;
                    }
                    break;
                case ACTION_AUDIO_BECOMING_NOISY:
                    if (!mIsPaused) {
                        message.what = WHAT_PAUSE_PLAYER;
                    }
                    break;
                case ACTION_CLEAR_NOTIFICATION:
                    message.what = WHAT_CLEAR_PLAYER;
                    break;
                default:
                    break;
            }
            gotoIdleState();
            mPlayerHandler.removeCallbacksAndMessages(null);
            mPlayerHandler.sendMessageDelayed(message, MESSAGE_DELAY_MILLI);
        }
        return START_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer error occurred : " + what + " => reset mediaPlayer");
        initializeMediaPlayer();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // release lock on wifi.
        mWifiLock.release();
        nextTrack();
        gotoIdleState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // broadcast event
        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_SEEK_COMPLETE);
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_SEEK, mp.getCurrentPosition());
        mLocalBroadcastManager.sendBroadcast(intent);
    }


    /**
     * Pause the playback.
     */
    @SuppressWarnings("deprecation")
    private void pause() {
        if (mHasAlreadyPlayed && !mIsPaused) {
            mIsPaused = true;
            mMediaPlayer.pause();

            // broadcast event
            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_PLAYER_PAUSED);
            mLocalBroadcastManager.sendBroadcast(intent);

            updateNotification();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            }
        }
    }

    /**
     * Resume the playback.
     */
    @SuppressWarnings("deprecation")
    private void resume() {
        if (mIsPaused) {
            mIsPaused = false;
            mMediaPlayer.start();

            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK,
                    mSimpleSoundCloudPlayerPlaylist.getCurrentTrack());
            mLocalBroadcastManager.sendBroadcast(intent);

            updateNotification();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void stopPlayer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        }
        mMediaPlayer.stop();
        mIsPaused = true;
    }

    private void nextTrack() {
        playTrack(mSimpleSoundCloudPlayerPlaylist.next());
    }

    private void previousTrack() {
        playTrack(mSimpleSoundCloudPlayerPlaylist.previous());
    }

    private void seekToPosition(int milli) {
        mMediaPlayer.seekTo(milli);
    }

    private void initializeMediaPlayer() {
        mMediaPlayer.reset();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
    }

    /**
     * Play a track.
     * <p/>
     * This method ensures that the media player will be in the right state to be able to play a new
     * datasource.
     *
     * @param track track url.
     */
    private void playTrack(SoundCloudTrack track) {
        try {
            // acquire lock on wifi.
            mWifiLock.acquire();

            // set media player to stop state in order to be able to call prepare.
            mMediaPlayer.reset();

            // set new data source
            mMediaPlayer.setDataSource(track.getStreamUrl() + SOUND_CLOUD_CLIENT_ID_PARAM
                    + mSoundCloundClientId);


            // update notification to avoid waiting for playback preparation
            // this improve the global user experience
            mIsPaused = false;
            updateNotification();

            // prepare synchronously as the service run on it's own handler thread.
            mMediaPlayer.prepare();

            // start the playback.
            mMediaPlayer.start();

            mHasAlreadyPlayed = true;

            // broadcast event
            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK, track);
            mLocalBroadcastManager.sendBroadcast(intent);

            updateMetaData(track);
        } catch (IOException e) {
            Log.e(TAG, "File referencing not exist : " + track);
        }
    }

    /**
     * Update the notification with the current track information.
     */
    private void updateNotification() {
        mSimpleSoundCloudNotificationManager.notify(this, mSimpleSoundCloudPlayerPlaylist.getCurrentTrack(), mIsPaused);
    }

    /**
     * Initialize the handler used to stop the service when idle period ends.
     *
     * @param looper from a non ui-thread
     */
    private void initializeStopHandler(Looper looper) {
        mStopServiceHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what != WHAT_RELEASE_PLAYER || !mIsPaused) {
                    return;
                }

                stopSelf();
            }
        };
    }

    /**
     * Start idle state.
     * <p/>
     * After {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer#IDLE_PERIOD_MILLI}
     * the service will ne stopped.
     */
    private void gotoIdleState() {
        mStopServiceHandler.removeCallbacksAndMessages(null);
        mStopServiceHandler.sendEmptyMessageDelayed(WHAT_RELEASE_PLAYER, IDLE_PERIOD_MILLI);
    }

    /**
     * Initialize the remote control client on the lock screen.
     */
    @SuppressWarnings("deprecation")
    private void initLockScreenRemoteControlClient() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mMediaButtonReceiverComponent = new ComponentName(
                    this.getPackageName(), SimpleSoundCloudMediaButtonReceiver.class.getName());
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

            if (mRemoteControlClientCompat == null) {
                Intent remoteControlIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                remoteControlIntent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClientCompat = new RemoteControlClientCompat(
                        PendingIntent.getBroadcast(this, 0, remoteControlIntent, 0));
                RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);

            }
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClientCompat.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                    | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                    | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                    | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
        }
    }

    /**
     * Update meta data used by the remote control client.
     *
     * @param track track currently played.
     */
    @SuppressWarnings("deprecation")
    private void updateMetaData(SoundCloudTrack track) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, track.getTitle())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, track.getArtist())
                    .apply();
        }
    }

    /**
     * Looper used process player request.
     */
    private final class PlayerHandler extends Handler {

        /**
         * Handler used to process player request.
         *
         * @param looper must not be null.
         */
        public PlayerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case WHAT_PLAY:
                    playTrack(((SoundCloudTrack) data.getParcelable(BUNDLE_KEY_SOUND_CLOUD_TRACK)));
                    break;
                case WHAT_PAUSE_PLAYER:
                    pause();
                    break;
                case WHAT_RESUME_PLAYER:
                    resume();
                    break;
                case WHAT_STOP_PLAYER:
                    stopPlayer();
                    break;
                case WHAT_NEXT_TRACK:
                    nextTrack();
                    break;
                case WHAT_PREVIOUS_TRACK:
                    previousTrack();
                    break;
                case WHAT_SEEK_TO:
                    seekToPosition(data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION));
                    break;
                case WHAT_CLEAR_PLAYER:
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    }
}
