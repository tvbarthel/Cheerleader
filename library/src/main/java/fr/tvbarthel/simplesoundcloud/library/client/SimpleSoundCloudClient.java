package fr.tvbarthel.simplesoundcloud.library.client;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser;
import fr.tvbarthel.simplesoundcloud.library.offline.Offliner;
import retrofit.RestAdapter;
import rx.Observable;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class SimpleSoundCloudClient {

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
    private static SimpleSoundCloudClient sInstance;

    /**
     * Rest adapter used to create {@link RetrofitService}
     */
    private RestAdapter mRestAdapter;

    /**
     * "Retrofit service" which encapsulate communication with sound cloud api.
     */
    private RetrofitService mRetrofitService;

    /**
     * {@link retrofit.RequestInterceptor} used to sign each request with sound cloud client id.
     */
    private RequestSignator mRequestSignator;

    /**
     * WeakReference on the application context.
     */
    private WeakReference<Context> mApplicationContext;

    /**
     * Sound cloud client id used to have access to the API.
     */
    private String mClientKey;

    /**
     * Used to know if the current client instance has been closed.
     */
    private boolean mIsClosed;

    /**
     * Private default constructor.
     */
    private SimpleSoundCloudClient() {

    }

    /**
     * Singleton pattern.
     *
     * @param applicationContext context used to initiate
     *                           {@link fr.tvbarthel.simplesoundcloud.library.offline.Offliner}
     * @param clientId           SoundCloud api client key.
     */
    private SimpleSoundCloudClient(Context applicationContext, String clientId) {

        mClientKey = clientId;
        mIsClosed = false;

        mRequestSignator = new RequestSignator(mClientKey);

        mApplicationContext = new WeakReference<>(applicationContext);

        /**
         * Initialize the Retrofit adapter for network communication.
         */
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(SOUND_CLOUD_API)
                .setRequestInterceptor(mRequestSignator)
                .build();
        mRetrofitService = mRestAdapter.create(RetrofitService.class);

        /**
         * Initialize the Offliner component for offline storage.
         */
        Offliner.initInstance(getContext(), false);
    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param context  context used to instantiate internal components, no hard reference will be kept.
     * @param clientId sound cloud client id.
     * @return instance of {@link SimpleSoundCloudClient}
     */
    private static SimpleSoundCloudClient getInstance(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null || sInstance.mIsClosed) {
            sInstance = new SimpleSoundCloudClient(context.getApplicationContext(), clientId);
        } else {
            sInstance.mRequestSignator.setClientId(clientId);
            sInstance.mClientKey = clientId;
        }
        return sInstance;
    }

    /**
     * Release resources associated with this client.
     */
    public void close() {
        if (mIsClosed) {
            return;
        }
        mIsClosed = true;

        mRestAdapter = null;
        mRetrofitService = null;
        mRequestSignator = null;

        mApplicationContext.clear();
        mApplicationContext = null;

        mClientKey = null;
    }

    /**
     * Retrieve SoundCloud user profile.
     *
     * @param userId user id.
     * @return {@link rx.Observable} on {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser}
     */
    public Observable<SoundCloudUser> getUser(int userId) {
        checkState();
        return mRetrofitService.getUser(String.valueOf(userId))
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_USER);
    }

    /**
     * Retrieve SoundCloud user profile.
     *
     * @param userName user name.
     * @return {@link rx.Observable} on {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser}
     */
    public Observable<SoundCloudUser> getUser(String userName) {
        checkState();
        return mRetrofitService.getUser(userName)
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_USER);
    }

    /**
     * Retrieve the public tracks of a user.
     *
     * @param userName user name.
     * @return {@link rx.Observable} on an ArrayList of
     */
    public Observable<ArrayList<SoundCloudTrack>> getUserTracks(String userName) {
        checkState();
        return mRetrofitService.getUserTracks(userName)
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_USER_TRACKS);
    }

    /**
     * Retrieve a SoundCloud track according to its id.
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable} on {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack}
     */
    public Observable<SoundCloudTrack> getTrack(int trackId) {
        checkState();
        return mRetrofitService.getTrack(trackId)
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_TRACK);
    }

    /**
     * Define the log policy.
     * <p/>
     * Note : some log configuration can increase memory foot print and/or reduce the performance.
     * Use them with caution.
     * <p/>
     * {@link SimpleSoundCloudClient#LOG_NONE}
     * {@link SimpleSoundCloudClient#LOG_RETROFIT}
     * {@link SimpleSoundCloudClient#LOG_OFFLINER}
     * <p/>
     * Different log policies can be combine :
     * <pre>
     * simpleSoundCloud.setLog(SimpleSoundCloud.LOG_OFFLINER | SimpleSoundCloud.LOG_RETROFIT);
     * </pre>
     *
     * @param logLevel log policy.
     */
    private void setLog(int logLevel) {
        checkState();
        if ((logLevel & LOG_RETROFIT) != 0) {
            mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        } else {
            mRestAdapter.setLogLevel(RestAdapter.LogLevel.NONE);
        }
        Offliner.debug((logLevel & LOG_OFFLINER) != 0);

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
     * Used to check the state of the client instance.
     */
    private void checkState() {
        if (mIsClosed) {
            throw new IllegalStateException("Client instance can't be used after being closed.");
        }
    }


    /**
     * Builder used to build a {@link SimpleSoundCloudClient}
     */
    public static class Builder {

        private Context context;
        private String apiKey;
        private int logLevel;

        /**
         * Default constructor.
         */
        public Builder() {
            logLevel = LOG_NONE;
        }

        /**
         * Context from which the client will be build.
         *
         * @param context context used to instantiate internal components.
         * @return {@link SimpleSoundCloudClient.Builder}
         */
        public Builder from(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Api key with which SoundCloud call will be performed.
         *
         * @param apiKey sound cloud api key.
         * @return {@link SimpleSoundCloudClient.Builder}
         */
        public Builder with(String apiKey) {
            if (apiKey == null) {
                throw new IllegalArgumentException("SoundCloud api can't be null");
            }
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Define the log policy.
         * <p/>
         * Note : some log configuration can increase memory foot print and/or reduce the performance.
         * Use them with caution.
         * <p/>
         * {@link SimpleSoundCloudClient#LOG_NONE}
         * {@link SimpleSoundCloudClient#LOG_RETROFIT}
         * {@link SimpleSoundCloudClient#LOG_OFFLINER}
         * <p/>
         * Different log policies can be combine :
         * <pre>
         * simpleSoundCloud.setLog(SimpleSoundCloud.LOG_OFFLINER | SimpleSoundCloud.LOG_RETROFIT);
         * </pre>
         *
         * @param logLevel log policy.
         * @return {@link SimpleSoundCloudClient.Builder}
         */
        public Builder log(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        /**
         * Build the client.
         *
         * @return {@link SimpleSoundCloudClient}
         */
        public SimpleSoundCloudClient build() {
            if (this.context == null) {
                throw new IllegalStateException("Context should be passed using 'Builder.from' to build the client.");
            }

            if (this.apiKey == null) {
                throw new IllegalStateException("Api key should be passed using 'Builder.with' to build the client.");
            }

            SimpleSoundCloudClient instance = getInstance(this.context, this.apiKey);
            if (!this.apiKey.equals(instance.mClientKey)) {
                throw new IllegalStateException("Only one api key can be used at the same time.");
            }

            if (logLevel != LOG_NONE) {
                sInstance.setLog(logLevel);
            }

            return sInstance;
        }
    }

}
