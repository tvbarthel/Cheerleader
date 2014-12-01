package fr.tvbarthel.simplesoundcloud.library;

/**
 * Encapsulate network and player features to work with sound cloud.
 */
public final class SimpleSoundCloud {

    /**
     * Instance, singleton pattern.
     */
    private static SimpleSoundCloud sInstance;

    /**
     * "Retrofit service" which encapsulate communication with sound cloud api.
     */
    private SimpleSoundCloudService mSimpleSoundCloudService;

    /**
     * Non instantiable class.
     */
    private SimpleSoundCloud() {
    }

    /**
     * Retrieve the single instance.
     *
     * @return instance of {@link fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud}
     */
    public static SimpleSoundCloud getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleSoundCloud();
        }
        return sInstance;
    }
}
