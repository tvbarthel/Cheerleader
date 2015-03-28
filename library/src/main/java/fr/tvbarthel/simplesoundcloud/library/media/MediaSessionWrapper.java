package fr.tvbarthel.simplesoundcloud.library.media;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.remote.RemoteControlClientCompat;
import fr.tvbarthel.simplesoundcloud.library.remote.RemoteControlHelper;

/**
 * Wrapper used to encapsulate {@link android.support.v4.media.session.MediaSessionCompat} behaviour
 * as well as a remote control client for lock screen on pre Lollipop.
 */
public class MediaSessionWrapper {

    /**
     * Stopped state.
     * See also :
     * {@link android.support.v4.media.session.PlaybackStateCompat#STATE_STOPPED}
     * {@link android.media.RemoteControlClient#PLAYSTATE_STOPPED}
     */
    public static final int PLAYBACK_STATE_STOPPED = 0x00000000;

    /**
     * Playing state.
     * See also :
     * {@link android.support.v4.media.session.PlaybackStateCompat#STATE_PLAYING}
     * {@link android.media.RemoteControlClient#PLAYSTATE_PLAYING}
     */
    public static final int PLAYBACK_STATE_PLAYING = 0x00000001;

    /**
     * Paused state.
     * See also :
     * {@link android.support.v4.media.session.PlaybackStateCompat#STATE_PAUSED}
     * {@link android.media.RemoteControlClient#PLAYSTATE_PAUSED}
     */
    public static final int PLAYBACK_STATE_PAUSED = 0x00000002;

    /**
     * Action used to catch broadcast from {@link MediaSessionReceiver}
     */
    static final String ACTION_TOGGLE_PLAYBACK = "fr.tvbarthel.simplesoundcloud.library.media.TOGGLE_PLAYBACK";

    /**
     * Action used to catch broadcast from {@link .MediaSessionReceiver}
     */
    static final String ACTION_NEXT_TRACK = "fr.tvbarthel.simplesoundcloud.library.media.NEXT_TRACK";

    /**
     * Action used to catch broadcast from {@link MediaSessionReceiver}
     */
    static final String ACTION_PREVIOUS_TRACK = "fr.tvbarthel.simplesoundcloud.library.media.PREVIOUS_TRACK";

    /**
     * Tag.
     */
    private static final String TAG = MediaSessionWrapper.class.getSimpleName();

    /**
     * Media session used to interact with media controllers, volume key and media buttons.
     */
    private MediaSessionCompat mMediaSession;

    /**
     * Component name used to register receiver which catch lock screen remote control client
     * on pre Lollipop.
     */
    private ComponentName mMediaButtonReceiverComponent;

    /**
     * Remote control client used on pre Lollipop.
     */
    private RemoteControlClientCompat mRemoteControlClientCompat;

    /**
     * Current callback object.
     */
    private MediaSessionWrapperCallback mCallback;

    /**
     * Audio manager used to catch audio focus event.
     */
    private AudioManager mAudioManager;

    /**
     * App package name at runtime used for the Component name.
     */
    private String mRuntimePackageName;

    /**
     * Receiver used to catch lock screen event on pre Lollipop devices.
     */
    private LockScreenReceiver mLockScreenReceiver;

    /**
     * Used to register/unregister lock screen receiver.
     */
    private LocalBroadcastManager mLocalBroadcastManager;

    private Context mContext;

    /**
     * Wrapper used to encapsulate {@link android.support.v4.media.session.MediaSessionCompat} behaviour
     * as well as a remote control client for lock screen on pre Lollipop.
     *
     * @param context      holding context.
     * @param callback     callback used to catch media session or lock screen events.
     * @param audioManager audio manager used to request the focus.
     */
    public MediaSessionWrapper(Context context, MediaSessionWrapperCallback callback, AudioManager audioManager) {
        mContext = context;
        mCallback = callback;
        mAudioManager = audioManager;

        mRuntimePackageName = context.getPackageName();

        initLockScreenRemoteControlClient(context);

        mMediaSession = new MediaSessionCompat(context, TAG);
        mMediaSession.setCallback(new MediaSessionCallback());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    /**
     * Should be called to released the internal component.
     */
    @SuppressWarnings("deprecation")
    public void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);
            mLocalBroadcastManager.unregisterReceiver(mLockScreenReceiver);
        }
        mMediaSession.release();
    }

    /**
     * Propagate the playback state to the media session and the lock screen remote control.
     * <p/>
     * See also :
     * {@link fr.tvbarthel.simplesoundcloud.library.media.MediaSessionWrapper#PLAYBACK_STATE_STOPPED}
     * {@link fr.tvbarthel.simplesoundcloud.library.media.MediaSessionWrapper#PLAYBACK_STATE_PLAYING}
     * {@link fr.tvbarthel.simplesoundcloud.library.media.MediaSessionWrapper#PLAYBACK_STATE_PAUSED}
     *
     * @param state playback state.
     */
    @SuppressWarnings("deprecation")
    public void setPlaybackState(int state) {
        switch (state) {
            case PLAYBACK_STATE_STOPPED:
                setRemoteControlClientPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
                setMediaSessionCompatPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                break;
            case PLAYBACK_STATE_PLAYING:
                mMediaSession.setActive(true);
                setRemoteControlClientPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                setMediaSessionCompatPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            case PLAYBACK_STATE_PAUSED:
                setRemoteControlClientPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                setMediaSessionCompatPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            default:
                Log.e(TAG, "Unknown playback state.");
                break;
        }
    }

    /**
     * Update meta data used by the remote control client and the media session.
     *
     * @param track track currently played.
     */
    @SuppressWarnings("deprecation")
    public void setMetaData(SoundCloudTrack track) {
        setMetaData(track, null);
    }

    /**
     * Update meta data used by the remote control client and the media session.
     *
     * @param track   track currently played.
     * @param artwork track artwork.
     */
    @SuppressWarnings("deprecation")
    public void setMetaData(SoundCloudTrack track, Bitmap artwork) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            // set meta data on the lock screen for pre lollipop.
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            RemoteControlClientCompat.MetadataEditorCompat mediaEditorCompat
                    = mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, track.getTitle())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, track.getArtist());
            if (artwork != null) {
                mediaEditorCompat.putBitmap(
                        RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK, artwork);
            }
            mediaEditorCompat.apply();
        }

        // set meta data to the media session.
        MediaMetadataCompat.Builder metadataCompatBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
        if (artwork != null) {
            metadataCompatBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artwork);
        }
        mMediaSession.setMetadata(metadataCompatBuilder.build());
        setMediaSessionCompatPlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * Propagate playback state to the remote control client on the lock screen.
     *
     * @param state playback state.
     */
    private void setRemoteControlClientPlaybackState(int state) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mRemoteControlClientCompat.setPlaybackState(state);
        }
    }

    /**
     * Propagate playback state to the media session compat.
     *
     * @param state playback state.
     */
    private void setMediaSessionCompatPlaybackState(int state) {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        mMediaSession.setPlaybackState(stateBuilder.build());
    }

    /**
     * Initialize the remote control client on the lock screen.
     *
     * @param context holding context.
     */
    @SuppressWarnings("deprecation")
    private void initLockScreenRemoteControlClient(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mMediaButtonReceiverComponent = new ComponentName(
                    mRuntimePackageName, MediaSessionReceiver.class.getName());
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

            if (mRemoteControlClientCompat == null) {
                Intent remoteControlIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                remoteControlIntent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClientCompat = new RemoteControlClientCompat(
                        PendingIntent.getBroadcast(context, 0, remoteControlIntent, 0));
                RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);

            }
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClientCompat.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                    | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                    | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                    | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                    | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);

            registerLockScreenReceiver(context);
        }
    }

    /**
     * Register the lock screen receiver used to catch lock screen media buttons events.
     *
     * @param context holding context.
     */
    private void registerLockScreenReceiver(Context context) {
        mLockScreenReceiver = new LockScreenReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_PLAYBACK);
        intentFilter.addAction(ACTION_NEXT_TRACK);
        intentFilter.addAction(ACTION_PREVIOUS_TRACK);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mLocalBroadcastManager.registerReceiver(mLockScreenReceiver, intentFilter);
    }

    /**
     * Catch callback from media session.
     */
    private final class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            super.onPlay();
            mCallback.onPlay();
        }

        @Override
        public void onPause() {
            super.onPause();
            mCallback.onPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            mCallback.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            mCallback.onSkipToPrevious();
        }
    }

    /**
     * Catch callback from the {@link fr.tvbarthel.simplesoundcloud.library.media.MediaSessionReceiver}
     * which catch broadcast from the remote control client on the lock screen of pre Lollipop devices.
     */
    private final class LockScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                switch (intent.getAction()) {
                    case ACTION_TOGGLE_PLAYBACK:
                        mCallback.onPlayPauseToggle();
                        break;
                    case ACTION_NEXT_TRACK:
                        mCallback.onSkipToNext();
                        break;
                    case ACTION_PREVIOUS_TRACK:
                        mCallback.onSkipToPrevious();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Media session wrapper callbacks.
     */
    public interface MediaSessionWrapperCallback {

        /**
         * Called when play is requested.
         */
        public void onPlay();

        /**
         * Called when pause is requested.
         */
        public void onPause();

        /**
         * Called when next track should be played.
         */
        public void onSkipToNext();

        /**
         * Called when previous track should be played.
         */
        public void onSkipToPrevious();

        /**
         * Called when play/pause button is toggled.
         */
        public void onPlayPauseToggle();
    }
}
