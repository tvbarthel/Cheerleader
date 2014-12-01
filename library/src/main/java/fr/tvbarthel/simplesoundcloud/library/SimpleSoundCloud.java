package fr.tvbarthel.simplesoundcloud.library;

import retrofit.RestAdapter;

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
     * Non instantiable class.
     */
    private SimpleSoundCloud(String clientId) {
        mSimpleSoundCloudRequestSignator = new SimpleSoundCloudRequestSignator(clientId);

        RestAdapter restAdapter =
                new RestAdapter.Builder().setEndpoint(SOUND_CLOUD_API)
                        .setRequestInterceptor(mSimpleSoundCloudRequestSignator)
                        .build();

        mSimpleSoundCloudService = restAdapter.create(SimpleSoundCloudService.class);
    }

    /**
     * Simple Sound cloud client initialized with a client id.
     *
     * @param clientId sound cloud client id.
     * @return instance of {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud}
     */
    public static SimpleSoundCloud getInstance(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Sound cloud client id can't be null.");
        }
        if (sInstance == null) {
            sInstance = new SimpleSoundCloud(clientId);
        } else {
            sInstance.mSimpleSoundCloudRequestSignator.setClientId(clientId);
        }
        return sInstance;
    }
}
