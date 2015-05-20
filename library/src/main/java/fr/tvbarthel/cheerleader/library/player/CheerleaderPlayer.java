package fr.tvbarthel.cheerleader.library.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.cheerleader.library.R;
import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;
import rx.functions.Action1;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class CheerleaderPlayer implements Action1<ArrayList<SoundCloudTrack>> {

    private static final int STATE_STOPPED = 0x00000000;
    private static final int STATE_PAUSED = 0x00000001;
    private static final int STATE_PLAYING = 0x00000002;

    /**
     * Instance, singleton pattern.
     */
    private static CheerleaderPlayer sInstance;

    /**
     * WeakReference on the application context.
     */
    private WeakReference<Context> mApplicationContext;

    /**
     * Sound cloud client id used to have access to the API.
     */
    private String mClientKey;

    /**
     * Internal listener used to catch service callbacks
     */
    private PlaybackListener mInternalListener;

    /**
     * Listener which should be notified of playback events.
     */
    private ArrayList<CheerleaderPlayerListener> mCheerleaderPlayerListeners;

    /**
     * Listener which should be notified of playlist events.
     */
    private ArrayList<CheerleaderPlaylistListener> mCheerleaderPlaylistListeners;

    /**
     * Manage the playlist used by the player.
     */
    private PlayerPlaylist mPlayerPlaylist;

    /**
     * Manage the notification.
     */
    private NotificationManager mNotificationManager;

    /**
     * Used to know the player state
     */
    private int mState;

    /**
     * Used to know if the current client instance has been closed.
     */
    private boolean mIsClosed;

    /**
     * Used to know if the player destroy should be delayed. Is delayed, destroy will be done
     * once the {@link PlaybackService} stop self.
     */
    private boolean mDestroyDelayed;

    /**
     * Private default constructor.
     */
    private CheerleaderPlayer() {

    }

    /**
     * Singleton pattern.
     *
     * @param applicationContext context used to initiate
     *                           {@link fr.tvbarthel.cheerleader.library.offline.Offliner}
     * @param clientId           SoundCloud api client key.
     */
    private CheerleaderPlayer(Context applicationContext, String clientId) {

        mClientKey = clientId;
        mIsClosed = false;
        mDestroyDelayed = false;
        mState = STATE_STOPPED;
        mCheerleaderPlayerListeners = new ArrayList<>();
        mCheerleaderPlaylistListeners = new ArrayList<>();

        mApplicationContext = new WeakReference<>(applicationContext);

        mPlayerPlaylist = PlayerPlaylist.getInstance();
        mNotificationManager = NotificationManager.getInstance(getContext());

        initInternalListener(applicationContext);
    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param context  context used to instantiate internal components, no hard reference will be kept.
     * @param clientId sound cloud client id.
     * @return instance of {@link CheerleaderPlayer}
     */
    private static CheerleaderPlayer getInstance(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null || sInstance.mIsClosed) {
            sInstance = new CheerleaderPlayer(context.getApplicationContext(), clientId);
        } else {
            sInstance.mClientKey = clientId;
        }
        // reset destroy request each time an instance is requested.
        sInstance.mDestroyDelayed = false;
        return sInstance;
    }

    @Override
    public void call(ArrayList<SoundCloudTrack> soundCloudTracks) {
        addTracks(soundCloudTracks);
    }

    /**
     * Release resources associated with the player.
     */
    public void destroy() {
        if (mIsClosed) {
            return;
        }
        if (mState != STATE_STOPPED) {
            mDestroyDelayed = true;
            return;
        }
        mIsClosed = true;

        PlaybackService.unregisterListener(getContext(), mInternalListener);
        mInternalListener = null;

        mApplicationContext.clear();
        mApplicationContext = null;

        mClientKey = null;
        mPlayerPlaylist = null;
        mCheerleaderPlayerListeners.clear();
    }

    /**
     * Start the playback. First track of the queue will be played.
     * <p/>
     * If the SoundCloud player is currently paused, the current track will be restart at the stopped position.
     */
    public void play() {
        checkState();
        if (mState == STATE_PAUSED) {
            PlaybackService.resume(getContext(), mClientKey);
        } else if (mState == STATE_STOPPED) {
            SoundCloudTrack track = mPlayerPlaylist.getCurrentTrack();
            if (track != null) {
                PlaybackService.play(getContext(), mClientKey, track);
            } else {
                return;
            }
        }
        mState = STATE_PLAYING;
    }

    /**
     * Play a track at a given position in the player playlist.
     *
     * @param position position of the track in the playlist.
     */
    public void play(int position) {
        checkState();
        ArrayList<SoundCloudTrack> tracks = mPlayerPlaylist.getPlaylist().getTracks();
        if (position >= 0 && position < tracks.size()) {
            SoundCloudTrack trackToPlay = tracks.get(position);
            mPlayerPlaylist.setPlayingTrack(position);
            PlaybackService.play(getContext(), mClientKey, trackToPlay);
        }

    }

    /**
     * Play a track which have been added to the player playlist.
     * <p/>
     * See also {@link CheerleaderPlayer#addTrack(SoundCloudTrack)}
     * {@link CheerleaderPlayer#addTracks(List)}
     *
     * @param track the track to play.
     */
    public void play(SoundCloudTrack track) {
        checkState();
        ArrayList<SoundCloudTrack> tracks = mPlayerPlaylist.getPlaylist().getTracks();
        int position = tracks.indexOf(track);
        if (position > -1) {
            mPlayerPlaylist.setPlayingTrack(position);
            PlaybackService.play(getContext(), mClientKey, track);
        }
    }

    /**
     * Pause the playback.
     */
    public void pause() {
        checkState();
        if (mState == STATE_PLAYING) {
            PlaybackService.pause(getContext(), mClientKey);
            mState = STATE_PAUSED;
        }
    }

    /**
     * Toggle playback.
     * <p/>
     * Basically, pause the player if playing and play if paused.
     */
    public void togglePlayback() {
        switch (mState) {
            case STATE_STOPPED:
            case STATE_PAUSED:
                play();
                break;
            case STATE_PLAYING:
                pause();
                break;
            default:
                break;
        }
    }

    /**
     * Stop the current played track and load the next one if the playlist isn't empty.
     * <p/>
     * If the current played track is the last one, the first track will be loaded.
     *
     * @return false if current playlist is empty.
     */
    public boolean next() {
        checkState();
        if (mPlayerPlaylist.isEmpty()) {
            return false;
        }
        PlaybackService.play(getContext(), mClientKey, mPlayerPlaylist.next());
        return true;
    }

    /**
     * Stop the current played track and load the previous one.
     * <p/>
     * If the current played track is the first one, the last track will be loaded.
     *
     * @return false if current playlist is empty.
     */
    public boolean previous() {
        checkState();
        if (mPlayerPlaylist.isEmpty()) {
            return false;
        }
        PlaybackService.play(getContext(), mClientKey, mPlayerPlaylist.previous());
        return true;
    }

    /**
     * Seek to the precise track position.
     * <p/>
     * The current playing state of the SoundCloud player will be kept.
     * <p/>
     * If playing it remains playing, if paused it remains paused.
     *
     * @param milli time in milli of the position.
     */
    public void seekTo(int milli) {
        checkState();
        if (!mPlayerPlaylist.isEmpty()) {
            PlaybackService.seekTo(getContext(), mClientKey, milli);
        }
    }

    /**
     * Add a track to the current SoundCloud player playlist.
     * <p/>
     * See also {@link CheerleaderPlayer#addTrack(SoundCloudTrack, boolean)}
     *
     * @param track {@link fr.tvbarthel.cheerleader.library.client.SoundCloudTrack} to be
     *              added to the player.
     */
    public void addTrack(SoundCloudTrack track) {
        addTrack(track, false);
    }

    /**
     * Add a track to the current SoundCloud player playlist.
     *
     * @param track   {@link fr.tvbarthel.cheerleader.library.client.SoundCloudTrack} to be
     *                added to the player.
     * @param playNow true to play the track immediately.
     */
    public void addTrack(SoundCloudTrack track, boolean playNow) {
        checkState();
        mPlayerPlaylist.add(track);
        for (CheerleaderPlaylistListener listener : mCheerleaderPlaylistListeners) {
            listener.onTrackAdded(track);
        }
        if (playNow) {
            play(mPlayerPlaylist.size() - 1);
        }
    }

    /**
     * Add a list of track to thr current SoundCloud player playlist.
     *
     * @param tracks list of {@link fr.tvbarthel.cheerleader.library.client.SoundCloudTrack}
     *               to be added to the player.
     */
    public void addTracks(List<SoundCloudTrack> tracks) {
        checkState();
        for (SoundCloudTrack track : tracks) {
            addTrack(track);
        }
    }

    /**
     * Remove a track from the SoundCloud player playlist.
     * <p/>
     * If the track is currently played, it will be stopped before being removed.
     *
     * @param playlistIndex index of the track to be removed.
     */
    public void removeTrack(int playlistIndex) {
        checkState();
        SoundCloudTrack currentTrack = mPlayerPlaylist.getCurrentTrack();
        SoundCloudTrack removedTrack = mPlayerPlaylist.remove(playlistIndex);

        if (removedTrack == null) {
            // nothing removed
            return;
        }

        if (mPlayerPlaylist.isEmpty()) {
            // playlist empty after deletion, stop player;
            PlaybackService.stop(getContext(), mClientKey);
        } else if (currentTrack != null && currentTrack.equals(removedTrack) && mState == STATE_PLAYING) {
            // play next track if removed one was the current and playing
            play(mPlayerPlaylist.getCurrentTrackIndex());
        }

        for (CheerleaderPlaylistListener listener : mCheerleaderPlaylistListeners) {
            listener.onTrackRemoved(removedTrack, mPlayerPlaylist.isEmpty());
        }
    }

    /**
     * Used to know if the player is playing or not.
     *
     * @return true if the player is playing a track.
     */
    public boolean isPlaying() {
        return mState == STATE_PLAYING;
    }


    /**
     * Retrieve the current tracks added to the playlist.
     *
     * @return current tracks loaded into the player.
     */
    public ArrayList<SoundCloudTrack> getTracks() {
        checkState();
        // copy the playlist to avoid reordering, addition, deletion directly on the list.
        return new ArrayList<>(mPlayerPlaylist.getPlaylist().getTracks());
    }

    /**
     * Retrieve the current played track.
     *
     * @return current track.
     */
    public SoundCloudTrack getCurrentTrack() {
        checkState();
        return mPlayerPlaylist.getCurrentTrack();
    }

    /**
     * Register a listener to catch player events.
     *
     * @param listener listener to register.
     */
    public void registerPlayerListener(CheerleaderPlayerListener listener) {
        checkState();
        mCheerleaderPlayerListeners.add(listener);
        if (mState == STATE_PLAYING) {
            listener.onPlayerPlay(mPlayerPlaylist.getCurrentTrack(), mPlayerPlaylist.getCurrentTrackIndex());
        } else if (mState == STATE_PAUSED) {
            listener.onPlayerPause();
        }
    }

    /**
     * Unregister listener used to catch player events.
     *
     * @param listener listener to unregister.
     */
    public void unregisterPlayerListener(CheerleaderPlayerListener listener) {
        checkState();
        mCheerleaderPlayerListeners.remove(listener);
    }

    /**
     * Register a listener to catch playlist events.
     *
     * @param listener listener to register.
     */
    public void registerPlaylistListener(CheerleaderPlaylistListener listener) {
        checkState();
        mCheerleaderPlaylistListeners.add(listener);
    }

    /**
     * Unregister listener used to catch playlist events.
     *
     * @param listener listener to unregister.
     */
    public void unregisterPlaylistListener(CheerleaderPlaylistListener listener) {
        checkState();
        mCheerleaderPlaylistListeners.remove(listener);
    }

    /**
     * Retrieve the context used at the creation.
     *
     * @return context.
     */
    private Context getContext() {
        if (mApplicationContext.get() == null) {
            throw new IllegalStateException("WeakReference on application context null");
        }
        return mApplicationContext.get();
    }

    /**
     * Initialize the internal listener.
     *
     * @param context context used to register the internal listener.
     */
    private void initInternalListener(Context context) {
        mInternalListener = new PlaybackListener() {
            @Override
            protected void onPlay(SoundCloudTrack track) {
                super.onPlay(track);
                mState = STATE_PLAYING;
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onPlayerPlay(track, mPlayerPlaylist.getCurrentTrackIndex());
                }
            }

            @Override
            protected void onPause() {
                super.onPause();
                mState = STATE_PAUSED;
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onPlayerPause();
                }
            }

            @Override
            protected void onPlayerDestroyed() {
                super.onPlayerDestroyed();
                mState = STATE_STOPPED;
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onPlayerDestroyed();
                }
                if (mDestroyDelayed) {
                    CheerleaderPlayer.this.destroy();
                }
            }

            @Override
            protected void onSeekTo(int milli) {
                super.onSeekTo(milli);
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onPlayerSeekTo(milli);
                }
            }

            @Override
            protected void onBufferingStarted() {
                super.onBufferingStarted();
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onBufferingStarted();
                }
            }

            @Override
            protected void onBufferingEnded() {
                super.onBufferingEnded();
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onBufferingEnded();
                }
            }

            @Override
            protected void onProgressChanged(int milli) {
                super.onProgressChanged(milli);
                for (CheerleaderPlayerListener listener : mCheerleaderPlayerListeners) {
                    listener.onProgressChanged(milli);
                }
            }
        };
        PlaybackService.registerListener(context, mInternalListener);
    }

    /**
     * Used to check the state of the client instance.
     */
    private void checkState() {
        if (mIsClosed) {
            throw new IllegalStateException("Client instance can't be used after being closed.");
        }
    }

    /**
     * Define the {@link NotificationConfig}
     * which will used to configure the playback notification.
     *
     * @param config started activity.
     */
    private void setNotificationConfig(NotificationConfig config) {
        mNotificationManager.setNotificationConfig(config);
    }

    /**
     * Builder used to build a {@link CheerleaderPlayer}
     */
    public static class Builder {

        private Context context;
        private String apiKey;
        private NotificationConfig notificationConfig;

        /**
         * Default constructor.
         */
        public Builder() {
            notificationConfig = new NotificationConfig();
            notificationConfig.setNotificationIcon(R.drawable.simple_sound_cloud_notification_icon);
            notificationConfig.setNotificationIconBackground(R.drawable.notification_icon_background);
        }

        /**
         * Context from which the client will be build.
         *
         * @param context context used to instantiate internal components.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder from(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Api key with which SoundCloud call will be performed.
         *
         * @param apiKey sound cloud api key.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder with(String apiKey) {
            if (apiKey == null) {
                throw new IllegalArgumentException("SoundCloud api can't be null");
            }
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Api key with which SoundCloud call will be performed.
         *
         * @param resId res id of sound cloud api key.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder with(@StringRes int resId) {
            if (context == null) {
                throw new IllegalStateException("Context should be set first.");
            }

            this.apiKey = context.getString(resId);
            return this;
        }

        /**
         * Define the drawable used as icon in the notification displayed while playing.
         *
         * @param resId icon res id.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder notificationIcon(@DrawableRes int resId) {
            notificationConfig.setNotificationIcon(resId);
            return this;
        }

        /**
         * Define the background of the notification icon.
         * <p/>
         * Only for Lollipop device.
         *
         * @param resId notification icon background.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder notificationIconBackground(@DrawableRes int resId) {
            notificationConfig.setNotificationIconBackground(resId);
            return this;
        }

        /**
         * Define the activity which will be started when the user touches the player notification.
         * <p/>
         * This activity should display a media controller.
         *
         * @param activity started activity.
         * @return {@link CheerleaderPlayer.Builder}
         */
        public Builder notificationActivity(Class<? extends Activity> activity) {
            notificationConfig.setNotificationActivity(activity);
            return this;
        }

        /**
         * Build the client.
         *
         * @return {@link CheerleaderPlayer}
         */
        public CheerleaderPlayer build() {
            if (this.context == null) {
                throw new IllegalStateException("Context should be passed using 'Builder.from' to build the client.");
            }

            if (this.apiKey == null) {
                throw new IllegalStateException("Api key should be passed using 'Builder.with' to build the client.");
            }

            CheerleaderPlayer instance = getInstance(this.context, this.apiKey);
            if (!this.apiKey.equals(instance.mClientKey)) {
                throw new IllegalStateException("Only one api key can be used at the same time.");
            }

            sInstance.setNotificationConfig(notificationConfig);

            return sInstance;
        }
    }

}
