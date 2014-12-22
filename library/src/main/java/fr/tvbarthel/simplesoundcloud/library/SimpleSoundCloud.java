package fr.tvbarthel.simplesoundcloud.library;

import android.content.Context;

import java.lang.ref.WeakReference;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser;
import fr.tvbarthel.simplesoundcloud.library.offline.SimpleSoundCloudOffliner;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudListener;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer;
import retrofit.RestAdapter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class SimpleSoundCloud {

    /**
     * Disable all logs.
     */
    public static final int LOG_NONE = 0x00000000;

    /**
     * Enable Retrofit log using RestAdapter.LogLevel.FULL.
     * <p/>
     * Log the headers, body, and metadata for both requests and responses.
     * <p/>
     * Note: This requires that the entire request and response body be buffered in memory!
     */
    public static final int LOG_RETROFIT = 0x00000001;

    /**
     * Enable log for Offliner part.
     * <p/>
     * Log is the content is save for offline or retrieved due to no network access as well as the
     * saving strategy (insertion or update).
     */
    public static final int LOG_OFFLINER = 0x00000010;

    /**
     * Sound cloud api url.
     */
    private static final String SOUND_CLOUD_API = "https://api.soundcloud.com/";

    /**
     * Instance, singleton pattern.
     */
    private static SimpleSoundCloud sInstance;

    /**
     * Rest adapter used to create {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloudService}
     */
    private RestAdapter mRestAdapter;

    /**
     * "Retrofit service" which encapsulate communication with sound cloud api.
     */
    private SimpleSoundCloudService mSimpleSoundCloudService;

    /**
     * {@link retrofit.RequestInterceptor} used to sign each request with sound cloud client id.
     */
    private SimpleSoundCloudRequestSignator mSimpleSoundCloudRequestSignator;

    /**
     * WeakReference on the application context.
     */
    private WeakReference<Context> mApplicationContext;

    /**
     * Sound cloud client id used to have access to the API.
     */
    private String mClientKey;

    /**
     * Singleton pattern.
     *
     * @param applicationContext context used to initiate
     *                           {@link fr.tvbarthel.simplesoundcloud.library.offline.SimpleSoundCloudOffliner}
     * @param clientId           SoundCloud api client key.
     */
    private SimpleSoundCloud(Context applicationContext, String clientId) {

        mClientKey = clientId;

        mSimpleSoundCloudRequestSignator = new SimpleSoundCloudRequestSignator(mClientKey);

        mApplicationContext = new WeakReference<>(applicationContext);

        /**
         * Initialize the Retrofit adapter for network communication.
         */
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(SOUND_CLOUD_API)
                .setRequestInterceptor(mSimpleSoundCloudRequestSignator)
                .build();
        mSimpleSoundCloudService = mRestAdapter.create(SimpleSoundCloudService.class);

        /**
         * Initialize the Offliner component for offline storage.
         */
        SimpleSoundCloudOffliner.initInstance(getContext(), false);

    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param context  context used to instantiate internal components, no hard reference will be kept.
     * @param clientId sound cloud client id.
     * @return instance of {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud}
     */
    public static SimpleSoundCloud getInstance(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null) {
            sInstance = new SimpleSoundCloud(context.getApplicationContext(), clientId);
        } else {
            sInstance.mSimpleSoundCloudRequestSignator.setClientId(clientId);
        }
        return sInstance;
    }

    /**
     * Retrieve SoundCloud user profile.
     *
     * @param userId user id.
     * @return {@link rx.Observable} on {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser}
     */
    public Observable<SoundCloudUser> getUser(int userId) {
        return mSimpleSoundCloudService.getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(SimpleSoundCloudOffliner.PREPARE_FOR_OFFLINE)
                .map(SimpleSoundCloudRxParser.PARSE_USER);
    }

    /**
     * Retrieve a SoundCloud track according to its id.
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable} on {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack}
     */
    public Observable<SoundCloudTrack> getTrack(int trackId) {
        return mSimpleSoundCloudService.getTrack(trackId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(SimpleSoundCloudOffliner.PREPARE_FOR_OFFLINE)
                .map(SimpleSoundCloudRxParser.PARSE_TRACK);
    }

    /**
     * Start the playback. First track of the queue will be played.
     * <p/>
     * If the SoundCloud player is currently paused, the current track will be restart at the stopped position.
     */
    public void play() {
        SimpleSoundCloudPlayer.play(getContext(), mClientKey);
    }

    /**
     * Pause the playback.
     */
    public void pause() {
        SimpleSoundCloudPlayer.pause(getContext(), mClientKey);
    }

    /**
     * Stop the current played track and load the next one if the playlist isn't empty.
     * <p/>
     * If the current played track is the last one, the first track will be loaded.
     */
    public void next() {
        SimpleSoundCloudPlayer.next(getContext(), mClientKey);
    }

    /**
     * Stop the current played track and load the previous one.
     * <p/>
     * If the current played track is the first one, the last track will be loaded.
     */
    public void previous() {
        SimpleSoundCloudPlayer.previous(getContext(), mClientKey);
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
        SimpleSoundCloudPlayer.seekTo(getContext(), mClientKey, milli);
    }

    /**
     * Add a track to the current SoundCloud player playlist.
     *
     * @param track   {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack} to be
     *                added to the player.
     * @param playNow true if the track should be played immediately,
     *                false to simple add the track to the queue.
     */
    public void addTrack(SoundCloudTrack track, boolean playNow) {
        SimpleSoundCloudPlayer.addTrack(getContext(), mClientKey, track, playNow);
    }

    /**
     * Remove a track from the SoundCloud player playlist.
     * <p/>
     * If the track is currently played, it will be stopped before being removed.
     *
     * @param playlistIndex index of the track to be removed.
     */
    public void removeTrack(int playlistIndex) {
        SimpleSoundCloudPlayer.removeTrack(getContext(), mClientKey, playlistIndex);
    }

    /**
     * Register a listener to catch player events.
     *
     * @param listener listener to register.
     */
    public void registerPlayerListener(SimpleSoundCloudListener listener) {
        SimpleSoundCloudPlayer.registerListener(getContext(), listener);
    }

    /**
     * Unregister listener used to catch player events.
     *
     * @param listener listener to unregister.
     */
    public void unregisterPlayerListener(SimpleSoundCloudListener listener) {
        SimpleSoundCloudPlayer.unregisterListener(getContext(), listener);
    }

    /**
     * Define the log policy.
     * <p/>
     * Note : some log configuration can increase memory foot print and/or reduce the performance.
     * Use them with caution.
     * <p/>
     * {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud#LOG_NONE}
     * {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud#LOG_RETROFIT}
     * {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud#LOG_OFFLINER}
     * <p/>
     * Different log policies can be combine :
     * <pre>
     * simpleSoundCloud.setLog(SimpleSoundCloud.LOG_OFFLINER | SimpleSoundCloud.LOG_RETROFIT);
     * </pre>
     *
     * @param logLevel log policy.
     */
    public void setLog(int logLevel) {

        if ((logLevel & LOG_RETROFIT) != 0) {
            mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        } else {
            mRestAdapter.setLogLevel(RestAdapter.LogLevel.NONE);
        }
        SimpleSoundCloudOffliner.debug((logLevel & LOG_OFFLINER) != 0);

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

}
