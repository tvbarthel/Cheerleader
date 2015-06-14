package fr.tvbarthel.cheerleader.library.client;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.SparseArray;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fr.tvbarthel.cheerleader.library.offline.Offliner;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Encapsulate network features used to support an artist on SoundCloud.
 */
public final class CheerleaderClient implements Closeable {

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
    private static CheerleaderClient sInstance;

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
     * Cheer artist on.
     */
    private String mArtistName;

    /**
     * Object used as cache RAM in order to avoid spamming API with multiple
     * call of {@link CheerleaderClient#getArtistProfile()}
     */
    private CacheRam mCacheRam;

    /**
     * Private default constructor.
     */
    private CheerleaderClient() {

    }

    /**
     * Singleton pattern.
     *
     * @param applicationContext context used to initiate
     *                           {@link fr.tvbarthel.cheerleader.library.offline.Offliner}
     * @param clientId           SoundCloud api client key.
     * @param artistName         sound cloud artiste name.
     */
    private CheerleaderClient(Context applicationContext, String clientId, String artistName) {

        mArtistName = artistName;
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

        mCacheRam = new CacheRam();
    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param context    context used to instantiate internal components, no hard reference will be kept.
     * @param clientId   sound cloud client id.
     * @param artistName sound cloud artiste name.
     * @return instance of {@link CheerleaderClient}
     */
    private static CheerleaderClient getInstance(Context context, String clientId, String artistName) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (artistName == null) {
            throw new IllegalArgumentException("Sound cloud artistName can't be null.");
        }
        if (sInstance == null || sInstance.mIsClosed) {
            sInstance = new CheerleaderClient(context.getApplicationContext(), clientId, artistName);
        } else {
            sInstance.mRequestSignator.setClientId(clientId);
            sInstance.mClientKey = clientId;
            sInstance.mArtistName = artistName;
        }
        return sInstance;
    }

    /**
     * Release resources associated with this client.
     * <p/>
     * {@inheritDoc}
     */
    @Override
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
     * Retrieve the public tracks of the supported artist.
     *
     * @return {@link rx.Observable} on an ArrayList of the artist's tracks.
     */
    public Observable<ArrayList<SoundCloudTrack>> getArtistTracks() {
        checkState();
        if (mCacheRam.tracks.size() != 0) {
            return Observable.create(new Observable.OnSubscribe<ArrayList<SoundCloudTrack>>() {
                @Override
                public void call(Subscriber<? super ArrayList<SoundCloudTrack>> subscriber) {
                    subscriber.onNext(mCacheRam.tracks);
                    subscriber.onCompleted();
                }
            });
        } else {
            return mRetrofitService.getUserTracks(mArtistName)
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_USER_TRACKS)
                .map(cacheTracks());
        }
    }

    /**
     * Retrieve SoundCloud artist profile.
     *
     * @return {@link rx.Observable} on {@link SoundCloudUser}
     */
    public Observable<SoundCloudUser> getArtistProfile() {
        checkState();
        if (mCacheRam.artistProfile != null) {
            return Observable.create(new Observable.OnSubscribe<SoundCloudUser>() {
                @Override
                public void call(Subscriber<? super SoundCloudUser> subscriber) {
                    subscriber.onNext(mCacheRam.artistProfile);
                    subscriber.onCompleted();
                }
            });
        } else {
            return mRetrofitService.getUser(mArtistName)
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_USER)
                .map(cacheArtistProfile());
        }
    }

    /**
     * Retrieve comments related to a track of the supported artist.
     *
     * @param track track of which comment are related.
     * @return {@link rx.Observable} on {@link java.util.ArrayList}
     * of {@link SoundCloudComment}
     */
    public Observable<ArrayList<SoundCloudComment>> getTrackComments(final SoundCloudTrack track) {
        checkState();
        if (mCacheRam.tracksComments.get(track.getId()) != null) {
            return Observable.create(new Observable.OnSubscribe<ArrayList<SoundCloudComment>>() {
                @Override
                public void call(Subscriber<? super ArrayList<SoundCloudComment>> subscriber) {
                    subscriber.onNext(mCacheRam.tracksComments.get(track.getId()));
                    subscriber.onCompleted();
                }
            });
        } else {
            return mRetrofitService.getTrackComments(track.getId())
                .compose(Offliner.PREPARE_FOR_OFFLINE)
                .map(RxParser.PARSE_COMMENTS)
                .map(cacheTrackComments());
        }
    }

    /**
     * Define the log policy.
     * <p/>
     * Note : some log configuration can increase memory foot print and/or reduce the performance.
     * Use them with caution.
     * <p/>
     * {@link CheerleaderClient#LOG_NONE}
     * {@link CheerleaderClient#LOG_RETROFIT}
     * {@link CheerleaderClient#LOG_OFFLINER}
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
     * "Cache" the artist profile retrieved from network in RAM
     * to avoid requesting SoundCloud API for next call.
     *
     * @return {@link rx.functions.Func1} used to save the retrieved artist
     */
    private Func1<SoundCloudUser, SoundCloudUser> cacheArtistProfile() {
        return new Func1<SoundCloudUser, SoundCloudUser>() {
            @Override
            public SoundCloudUser call(SoundCloudUser soundCloudUser) {
                mCacheRam.artistProfile = soundCloudUser;
                return soundCloudUser;
            }
        };
    }

    /**
     * "Cache" the comments linked to a track retrieved from network in RAM
     * to avoid requesting SoundCloud API for next call.
     *
     * @return {@link rx.functions.Func1} used to save the retrieved comments list
     */
    private Func1<ArrayList<SoundCloudComment>, ArrayList<SoundCloudComment>> cacheTrackComments() {
        return new Func1<ArrayList<SoundCloudComment>, ArrayList<SoundCloudComment>>() {
            @Override
            public ArrayList<SoundCloudComment> call(ArrayList<SoundCloudComment> trackComments) {
                if (trackComments.size() > 0) {
                    mCacheRam.tracksComments.put(trackComments.get(0).getTrackId(), trackComments);
                }
                return trackComments;
            }
        };
    }

    /**
     * "Cache" the tracks list of the supported artist retrieved from network in RAM
     * to avoid requesting SoundCloud API for next call.
     *
     * @return {@link rx.functions.Func1} used to save the retrieved tracks list
     */
    private Func1<ArrayList<SoundCloudTrack>, ArrayList<SoundCloudTrack>> cacheTracks() {
        return new Func1<ArrayList<SoundCloudTrack>, ArrayList<SoundCloudTrack>>() {
            @Override
            public ArrayList<SoundCloudTrack> call(ArrayList<SoundCloudTrack> soundCloudTracks) {
                if (soundCloudTracks.size() > 0) {
                    mCacheRam.tracks = soundCloudTracks;
                }
                return soundCloudTracks;
            }
        };
    }


    /**
     * Builder used to build a {@link CheerleaderClient}
     */
    public static class Builder {

        private Context context;
        private String apiKey;
        private String artistName;
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
         * @return {@link CheerleaderClient.Builder}
         */
        public Builder from(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Api key with which SoundCloud call will be performed.
         *
         * @param apiKey sound cloud api key.
         * @return {@link CheerleaderClient.Builder}
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
         * @return {@link CheerleaderClient.Builder}
         */
        public Builder with(@StringRes int resId) {
            if (context == null) {
                throw new IllegalStateException("Context should be set first.");
            }

            this.apiKey = context.getString(resId);
            return this;
        }

        /**
         * Set the SoundCloud name of the artist you would like to supports.
         *
         * @param artistName sound cloud artiste name.
         * @return {@link CheerleaderClient.Builder}
         */
        public Builder supports(String artistName) {
            if (artistName == null) {
                throw new IllegalStateException("Artist name can't be null");
            }
            this.artistName = artistName;
            return this;
        }

        /**
         * Define the log policy.
         * <p/>
         * Note : some log configuration can increase memory foot print and/or reduce the performance.
         * Use them with caution.
         * <p/>
         * {@link CheerleaderClient#LOG_NONE}
         * {@link CheerleaderClient#LOG_RETROFIT}
         * {@link CheerleaderClient#LOG_OFFLINER}
         * <p/>
         * Different log policies can be combine :
         * <pre>
         * simpleSoundCloud.setLog(SimpleSoundCloud.LOG_OFFLINER | SimpleSoundCloud.LOG_RETROFIT);
         * </pre>
         *
         * @param logLevel log policy.
         * @return {@link CheerleaderClient.Builder}
         */
        public Builder log(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        /**
         * Build the client.
         *
         * @return {@link CheerleaderClient}
         */
        public CheerleaderClient build() {
            if (this.context == null) {
                throw new IllegalStateException("Context should be passed using "
                    + "'Builder.from' to build the client.");
            }

            if (this.apiKey == null) {
                throw new IllegalStateException("Api key should be passed using "
                    + "'Builder.with' to build the client.");
            }

            if (this.artistName == null) {
                throw new IllegalStateException("Artist name should be passed using "
                    + "'Builder.supports' to build the client.");
            }

            CheerleaderClient instance
                = getInstance(this.context, this.apiKey, this.artistName);
            if (!this.apiKey.equals(instance.mClientKey)) {
                throw new IllegalStateException("Only one api key can be used at the same time.");
            }

            if (logLevel != LOG_NONE) {
                sInstance.setLog(logLevel);
            }

            return sInstance;
        }
    }

    /**
     * Used to cache retrieve object in RAM in order to avoid spamming SoundCloud API
     * to due multiple call.
     */
    private static final class CacheRam {
        private SoundCloudUser artistProfile;
        private SparseArray<ArrayList<SoundCloudComment>> tracksComments;
        private ArrayList<SoundCloudTrack> tracks;

        CacheRam() {
            tracksComments = new SparseArray<>();
            tracks = new ArrayList<>();
        }
    }

}
