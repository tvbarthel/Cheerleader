package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
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
import java.util.ArrayList;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudPlaylist;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

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
     * Action used to add a sound cloud track to the queue.
     */
    private static final String ACTION_ADD_TRACK = "sound_cloud_player_action_add_track";

    /**
     * Action used to remove a sound cloud track from the queue.
     */
    private static final String ACTION_REMOVE_TRACK = "sound_cloud_player_action_remove_track";

    /**
     * Action used to start the sound cloud player.
     */
    private static final String ACTION_START_PLAYER = "sound_cloud_player_start";

    /**
     * Action used to pause the sound cloud player.
     */
    private static final String ACTION_PAUSE_PLAYER = "sound_cloud_player_pause";

    /**
     * Action used to skip to the next track of the sound cloud player.
     */
    private static final String ACTION_NEXT_TRACK = "sound_cloud_player_next";

    /**
     * Action used to skip to the previous track of the sound cloud player.
     */
    private static final String ACTION_PREVIOUS_TRACK = "sound_cloud_player_previous";

    /**
     * Action used to change the cursor of the current track.
     */
    private static final String ACTION_SEEK_TO = "sound_cloud_player_seek_to";

    /**
     * Action used to broadcast the playlist.
     */
    private static final String ACTION_PLAYLIST_REQUESTED = "sound_cloud_broadcast_playlist";

    /**
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Bundle key used to pass "play now" policy.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW = "sound_cloud_player_bundle_key_play_now";

    /**
     * Bundle key used to pass track url.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK = "sound_cloud_player_bundle_key_track_url";

    /**
     * Bundle key used to pass a track index.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_INDEX = "sound_cloud_player_bundle_key_track_index";

    /**
     * Bundle key used to seek to a given position.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION = "sound_cloud_player_bundle_key_seek_to";

    /**
     * what id used to identify "start player" message.
     */
    private static final int WHAT_START_PLAYER = 0;

    /**
     * what id used to identify "pause player" message.
     */
    private static final int WHAT_PAUSE_PLAYER = 1;

    /**
     * what id used to identify "next track" message.
     */
    private static final int WHAT_NEXT_TRACK = 2;

    /**
     * what id used to identify "previous track" message.
     */
    private static final int WHAT_PREVIOUS_TRACK = 3;

    /**
     * what id used to identify "seek to" message.
     */
    private static final int WHAT_SEEK_TO = 4;

    /**
     * what id used to identify add track message.
     */
    private static final int WHAT_ADD_TRACK = 5;

    /**
     * what id used to identify remove track message.
     */
    private static final int WHAT_REMOVE_TRACK = 6;

    /**
     * what id used to identify playlist request
     */
    private static final int WHAT_BROADCAST_PLAYLIST = 7;

    /**
     * Log cat and thread name prefix.
     */
    private static final String TAG = SimpleSoundCloudPlayer.class.getSimpleName();

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
     * MediaPlayer used to play music.
     */
    private MediaPlayer mMediaPlayer;

    /**
     * List of all track.
     */
    private ArrayList<SoundCloudTrack> mPlaylist;

    /**
     * Index of the current track.
     */
    private int mCurrentTrackIndex;

    /**
     * Used to know if the player is paused.
     */
    private boolean mIsPaused;

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
     * Start the playback. First track of the queue will be played.
     * <p/>
     * If the SoundCloud player is currently paused, the current track will be restart at the stopped position.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void play(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_START_PLAYER);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Pause the SoundCloud payer.
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
     * Stop the current played track and load the next one if the playlist isn't empty.
     * <p/>
     * If the current played track is the last one, the first track will be loaded.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void next(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_NEXT_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        context.startService(intent);
    }

    /**
     * Stop the current played track and load the previous one.
     * <p/>
     * If the current played track is the first one, the last track will be loaded.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void previous(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PREVIOUS_TRACK);
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
     * Add a track to the player queue.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     * @param track    sound cloud track stream url to be played.
     * @param playNow  true to play the track immediately.
     */
    public static void addTrack(Context context, String clientId, SoundCloudTrack track, boolean playNow) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_ADD_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK, track);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW, playNow);
        context.startService(intent);
    }

    /**
     * Remove track from the player playlist.
     *
     * @param context    context from which the service will be started.
     * @param clientId   SoundCloud api client id.
     * @param trackIndex track index in the playlist to remove.
     */
    public static void removeTrack(Context context, String clientId, int trackIndex) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_REMOVE_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_INDEX, trackIndex);
        context.startService(intent);
    }

    /**
     * Request the current playlist asynchronously.
     *
     * @param context  context from which the service will be started.
     * @param clientId SoundCloud api client id.
     */
    public static void requestPlaylist(Context context, String clientId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PLAYLIST_REQUESTED);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
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
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_PLAYLIST_RETRIEVED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_PLAYER_PAUSED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_TRACK_ADDED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_TRACK_REMOVED);
        filter.addAction(SimpleSoundCloudListener.ACTION_ON_SEEK_COMPLETE);

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
        mPlaylist = new ArrayList<>();
        mCurrentTrackIndex = 0;

        initializeMediaPlayer();

        mWifiLock = ((WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        mSimpleSoundCloudNotificationManager = new SimpleSoundCloudNotificationManager(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
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
                case ACTION_START_PLAYER:
                    message.what = WHAT_START_PLAYER;
                    break;
                case ACTION_PAUSE_PLAYER:
                    message.what = WHAT_PAUSE_PLAYER;
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
                case ACTION_ADD_TRACK:
                    message.what = WHAT_ADD_TRACK;
                    break;
                case ACTION_REMOVE_TRACK:
                    message.what = WHAT_REMOVE_TRACK;
                    break;
                case ACTION_PLAYLIST_REQUESTED:
                    message.what = WHAT_BROADCAST_PLAYLIST;
                    break;
                case ACTION_TOGGLE_PLAYBACK:
                    if (mIsPaused) {
                        message.what = WHAT_START_PLAYER;
                    } else {
                        message.what = WHAT_PAUSE_PLAYER;
                    }
                    break;
                default:
                    break;
            }
            mPlayerHandler.sendMessage(message);
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
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // broadcast event
        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_SEEK_COMPLETE);
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_SEEK, mp.getCurrentPosition());
        mLocalBroadcastManager.sendBroadcast(intent);
    }


    private void startPlayer() {
        if (mIsPaused) {
            // if player is paused, restart the current track.
            mIsPaused = false;
            mMediaPlayer.start();

            // broadcast event
            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK, mPlaylist.get(mCurrentTrackIndex));
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_INDEX, mCurrentTrackIndex);
            mLocalBroadcastManager.sendBroadcast(intent);

            startForeground();

        } else if (mPlaylist.size() > 0) {
            // if not paused and playlist isn't empty, play the first track;
            mCurrentTrackIndex = 0;
            playTrack(mPlaylist.get(mCurrentTrackIndex));
        }
    }

    private void pausePlayer() {
        mIsPaused = true;
        mMediaPlayer.pause();

        // broadcast event
        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_PLAYER_PAUSED);
        mLocalBroadcastManager.sendBroadcast(intent);

        stopForeground();
    }

    private void nextTrack() {
        mCurrentTrackIndex = (mCurrentTrackIndex + 1) % mPlaylist.size();
        playTrack(mPlaylist.get(mCurrentTrackIndex));
    }

    private void previousTrack() {
        mCurrentTrackIndex = (mPlaylist.size() + mCurrentTrackIndex - 1) % mPlaylist.size();
        playTrack(mPlaylist.get(mCurrentTrackIndex));
    }

    private void seekToPosition(int milli) {
        mMediaPlayer.seekTo(milli);
    }

    /**
     * Add a track to the playlist.
     *
     * @param track   track url.
     * @param playNow if true, track will be added to the current position and the song will start to
     *                play immediately.
     */
    private void enqueueTrack(SoundCloudTrack track, boolean playNow) {

        int addedPosition = 0;
        if (playNow) {
            mPlaylist.add(++mCurrentTrackIndex, track);
            addedPosition = mCurrentTrackIndex;
            playTrack(mPlaylist.get(mCurrentTrackIndex));
        } else {
            mPlaylist.add(track);
            addedPosition = mPlaylist.size() - 1;
        }


        // broadcast event
        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_ADDED);
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK, mPlaylist.get(mCurrentTrackIndex));
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_INDEX, addedPosition);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void removeTrack(int trackIndex) {

        // check if track is in the playlist
        if (trackIndex >= 0 && trackIndex < mPlaylist.size()) {

            // stop the player when the removed track is also the current one.
            if (mCurrentTrackIndex == trackIndex) {
                mMediaPlayer.stop();
            }

            // remove the track from the playlist
            SoundCloudTrack removedTrack = mPlaylist.remove(trackIndex);

            // update the current index
            if (mPlaylist.size() == 0) {
                // last song has been removed
                mCurrentTrackIndex = 0;
            } else if (mCurrentTrackIndex == trackIndex) {
                // update current index and start the nex track if player wasn't paused.
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % mPlaylist.size();
                if (!mIsPaused) {
                    playTrack(mPlaylist.get(mCurrentTrackIndex));
                }
            } else if (mCurrentTrackIndex > trackIndex) {
                // update current track index if the removed one was before
                // in the playlist
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % mPlaylist.size();
            }

            // broadcast event
            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_REMOVED);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK, removedTrack);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_INDEX, trackIndex);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
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
            mMediaPlayer.setDataSource(track.getStreamUrl() + SOUND_CLOUD_CLIENT_ID_PARAM + mSoundCloundClientId);

            // prepare synchronously as the service run on it's own handler thread.
            mMediaPlayer.prepare();

            // start the playback.
            mMediaPlayer.start();

            mIsPaused = false;

            // broadcast event
            Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_TRACK_PLAYED);
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_TRACK, mPlaylist.get(mCurrentTrackIndex));
            intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_INDEX, mCurrentTrackIndex);
            mLocalBroadcastManager.sendBroadcast(intent);

            startForeground();

        } catch (IOException e) {
            Log.e(TAG, "File referencing not exist : " + track);
        }
    }

    /**
     * Make the service run in foreground with an ongoing notification.
     */
    private void startForeground() {
        SoundCloudTrack track = mPlaylist.get(mCurrentTrackIndex);
        mSimpleSoundCloudNotificationManager.notify(this, track, mIsPaused, false);
    }

    /**
     * Remove foreground state and allow simple_sound_cloud_notification to be canceled manually.
     */
    private void stopForeground() {
        stopForeground(false);
        SoundCloudTrack track = mPlaylist.get(mCurrentTrackIndex);
        mSimpleSoundCloudNotificationManager.notify(this, track, mIsPaused, true);
    }

    /**
     * Broadcast the current playlist.
     */
    private void broadcastPlaylist() {
        Intent intent = new Intent(SimpleSoundCloudListener.ACTION_ON_PLAYLIST_RETRIEVED);
        SoundCloudPlaylist playlist = new SoundCloudPlaylist();
        playlist.addAllTracks(mPlaylist);
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_PLAYLIST, playlist);
        intent.putExtra(SimpleSoundCloudListener.EXTRA_KEY_INDEX, mCurrentTrackIndex);
        mLocalBroadcastManager.sendBroadcast(intent);
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
                case WHAT_START_PLAYER:
                    startPlayer();
                    break;
                case WHAT_PAUSE_PLAYER:
                    pausePlayer();
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
                case WHAT_ADD_TRACK:
                    enqueueTrack(
                            ((SoundCloudTrack) data.getParcelable(BUNDLE_KEY_SOUND_CLOUD_TRACK)),
                            data.getBoolean(BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW)
                    );
                    break;
                case WHAT_REMOVE_TRACK:
                    removeTrack(data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_INDEX));
                    break;
                case WHAT_BROADCAST_PLAYLIST:
                    broadcastPlaylist();
                    break;
                default:
                    break;
            }
        }
    }
}
