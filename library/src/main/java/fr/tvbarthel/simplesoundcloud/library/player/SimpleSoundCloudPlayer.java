package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBarActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.simplesoundcloud.library.R;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudPlaylist;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import rx.functions.Action1;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class SimpleSoundCloudPlayer implements Action1<ArrayList<SoundCloudTrack>> {

    /**
     * Instance, singleton pattern.
     */
    private static SimpleSoundCloudPlayer sInstance;

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
    private SimpleSoundCloudListener mInternalListener;

    /**
     * Manage the playlist used by the player.
     */
    private PlayerPlaylist mPlayerPlaylist;

    /**
     * Manage the notification.
     */
    private NotificationManager mNotificationManager;

    /**
     * Used to know if the player is paused.
     */
    private boolean mIsPaused;

    /**
     * Used to know if the current client instance has been closed.
     */
    private boolean mIsClosed;


    /**
     * Private default constructor.
     */
    private SimpleSoundCloudPlayer() {

    }

    /**
     * Singleton pattern.
     *
     * @param applicationContext context used to initiate
     *                           {@link fr.tvbarthel.simplesoundcloud.library.offline.Offliner}
     * @param clientId           SoundCloud api client key.
     */
    private SimpleSoundCloudPlayer(Context applicationContext, String clientId) {

        mClientKey = clientId;
        mIsClosed = false;
        mIsPaused = false;

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
     * @return instance of {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer}
     */
    private static SimpleSoundCloudPlayer getInstance(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null || sInstance.mIsClosed) {
            sInstance = new SimpleSoundCloudPlayer(context.getApplicationContext(), clientId);
        } else {
            sInstance.mClientKey = clientId;
        }
        return sInstance;
    }

    @Override
    public void call(ArrayList<SoundCloudTrack> soundCloudTracks) {
        addTracks(soundCloudTracks);
    }

    /**
     * Release resources associated with this client.
     */
    public void close() {
        if (mIsClosed) {
            return;
        }
        mIsClosed = true;

        PlaybackService.unregisterListener(getContext(), mInternalListener);
        mInternalListener = null;

        mApplicationContext.clear();
        mApplicationContext = null;

        mClientKey = null;
        mPlayerPlaylist = null;
    }

    /**
     * Used to know if the player is playing or not.
     *
     * @return true if the player is playing a track.
     */
    public boolean isPlaying() {
        return !mIsPaused;
    }

    /**
     * Start the playback. First track of the queue will be played.
     * <p/>
     * If the SoundCloud player is currently paused, the current track will be restart at the stopped position.
     */
    public void play() {
        checkState();
        if (mIsPaused) {
            PlaybackService.resume(getContext(), mClientKey);
        } else {
            SoundCloudTrack track = mPlayerPlaylist.getCurrentTrack();
            if (track != null) {
                PlaybackService.play(getContext(), mClientKey, track);
            } else {
                return;
            }
        }
        mIsPaused = false;
    }

    /**
     * Pause the playback.
     */
    public void pause() {
        checkState();
        mIsPaused = true;
        PlaybackService.pause(getContext(), mClientKey);
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
     *
     * @param track {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack} to be
     *              added to the player.
     */
    public void addTrack(SoundCloudTrack track) {
        checkState();
        mPlayerPlaylist.add(track);
    }

    /**
     * Add a list of track to thr current SoundCloud player playlist.
     *
     * @param tracks list of {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack}
     *               to be added to the player.
     */
    public void addTracks(List<SoundCloudTrack> tracks) {
        checkState();
        mPlayerPlaylist.addAll(tracks);
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
        } else if (currentTrack.equals(removedTrack) && !mIsPaused) {
            // play next track if removed one was the current and playing
            play();
        }
    }

    /**
     * Retrieve the current playlist.
     *
     * @return current playlist.
     */
    public SoundCloudPlaylist getPlaylist() {
        checkState();
        return mPlayerPlaylist.getPlaylist();
    }

    /**
     * Register a listener to catch player events.
     *
     * @param listener listener to register.
     */
    public void registerPlayerListener(SimpleSoundCloudListener listener) {
        checkState();
        PlaybackService.registerListener(getContext(), listener);
    }

    /**
     * Unregister listener used to catch player events.
     *
     * @param listener listener to unregister.
     */
    public void unregisterPlayerListener(SimpleSoundCloudListener listener) {
        checkState();
        PlaybackService.unregisterListener(getContext(), listener);
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
        mInternalListener = new SimpleSoundCloudListener() {
            @Override
            protected void onPlay(SoundCloudTrack track) {
                super.onPlay(track);
                mIsPaused = false;
            }

            @Override
            protected void onPause() {
                super.onPause();
                mIsPaused = true;
            }

            @Override
            protected void onPlayerDestroyed() {
                super.onPlayerDestroyed();
                mIsPaused = false;
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
     * Builder used to build a {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer}
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
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
         */
        public Builder from(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Api key with which SoundCloud call will be performed.
         *
         * @param apiKey sound cloud api key.
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
         */
        public Builder with(String apiKey) {
            if (apiKey == null) {
                throw new IllegalArgumentException("SoundCloud api can't be null");
            }
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Define the drawable used as icon in the notification displayed while playing.
         *
         * @param resId icon res id.
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
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
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
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
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
         */
        public Builder notificationActivity(ActionBarActivity activity) {
            notificationConfig.setNotificationActivity(activity);
            return this;
        }

        /**
         * Define the activity which will be started when the user touches the player notification.
         * <p/>
         * This activity should display a media controller.
         *
         * @param activity started activity.
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer.Builder}
         */
        public Builder notificationActivity(Activity activity) {
            notificationConfig.setNotificationActivity(activity);
            return this;
        }


        /**
         * Build the client.
         *
         * @return {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer}
         */
        public SimpleSoundCloudPlayer build() {
            if (this.context == null) {
                throw new IllegalStateException("Context should be passed using 'Builder.from' to build the client.");
            }

            if (this.apiKey == null) {
                throw new IllegalStateException("Api key should be passed using 'Builder.with' to build the client.");
            }

            SimpleSoundCloudPlayer instance = getInstance(this.context, this.apiKey);
            if (!this.apiKey.equals(instance.mClientKey)) {
                throw new IllegalStateException("Only one api key can be used at the same time.");
            }

            sInstance.setNotificationConfig(notificationConfig);

            return sInstance;
        }
    }

}
