package fr.tvbarthel.simplesoundcloud.library;

import android.content.Context;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser;
import fr.tvbarthel.simplesoundcloud.library.offline.SimpleSoundCloudOffliner;
import retrofit.RestAdapter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class SimpleSoundCloud {

    /**
     * Sound cloud api url.
     */
    private static final String SOUND_CLOUD_API = "https://api.soundcloud.com/";

    /**
     * Instance, singleton pattern.
     */
    private static SimpleSoundCloud sInstance;

    /**
     * "Retrofit service" which encapsulate communication with sound cloud api.
     */
    private SimpleSoundCloudService mSimpleSoundCloudService;

    /**
     * {@link retrofit.RequestInterceptor} used to sign each request with sound cloud client id.
     */
    private SimpleSoundCloudRequestSignator mSimpleSoundCloudRequestSignator;

    /**
     * Singleton pattern.
     *
     * @param context  context used to initiate
     *                 {@link fr.tvbarthel.simplesoundcloud.library.offline.SimpleSoundCloudOffliner}
     * @param clientId SoundCloud api client key.
     */
    private SimpleSoundCloud(Context context, String clientId) {
        mSimpleSoundCloudRequestSignator = new SimpleSoundCloudRequestSignator(clientId);

        RestAdapter restAdapter
                = new RestAdapter.Builder().setEndpoint(SOUND_CLOUD_API)
                .setRequestInterceptor(mSimpleSoundCloudRequestSignator)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        mSimpleSoundCloudService = restAdapter.create(SimpleSoundCloudService.class);

        SimpleSoundCloudOffliner.initInstance(context, false);
    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param context  context used to instanciate internal components, no hard reference will be kept.
     * @param clientId sound cloud client id.
     * @return instance of {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud}
     */
    public static SimpleSoundCloud getInstance(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null) {
            sInstance = new SimpleSoundCloud(context, clientId);
        } else {
            sInstance.mSimpleSoundCloudRequestSignator.setClientId(clientId);
        }
        return sInstance;
    }

    /**
     * Retrieve SoundCloud user profile.
     *
     * @param userId user id.
     * @return {@link rx.Observable}
     */
    public Observable<SoundCloudUser> getUser(int userId) {
        return mSimpleSoundCloudService.getUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(SimpleSoundCloudOffliner.PREPARE_FOR_OFFLINE)
                .map(SimpleSoundCloudRxParser.PARSE_USER);
    }

}
