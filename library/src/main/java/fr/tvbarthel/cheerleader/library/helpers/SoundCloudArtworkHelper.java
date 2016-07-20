package fr.tvbarthel.cheerleader.library.helpers;

import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;
import fr.tvbarthel.cheerleader.library.client.SoundCloudUser;

/**
 * Used to encapsulate artwork url format.
 */
public final class SoundCloudArtworkHelper {

    /**
     * Artwork format : 16x16.
     */
    public static final String MINI = "mini";

    /**
     * Artwork format : 20x20.
     */
    public static final String TINY = "tiny";

    /**
     * Artwork format : 32x32.
     */
    public static final String SMALL = "small";

    /**
     * Artwork format : 47x47.
     */
    public static final String BADGE = "badge";

    /**
     * Artwork format : 100x100.
     */
    public static final String LARGE = "large";

    /**
     * Artwork format : 300x300.
     */
    public static final String XLARGE = "t300x300";

    /**
     * Artwork format : 400x400.
     */
    public static final String XXLARGE = "crop";

    /**
     * Artwork format : 500x500.
     */
    public static final String XXXLARGE = "t500x500";

    /**
     * Non instantiable class.
     */
    private SoundCloudArtworkHelper() {

    }

    /**
     * Retrieve the artwork url of a track pointing to the requested size.
     * <p/>
     * By default, {@link fr.tvbarthel.cheerleader.library.client.SoundCloudTrack#getArtworkUrl()}
     * points to the {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#LARGE}
     * <p/>
     * Available size are :
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#MINI}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#TINY}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#SMALL}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#BADGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#LARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XLARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XXLARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XXXLARGE}
     *
     * @param track track from which artwork url should be returned.
     * @param size  wished size.
     * @return artwork url or null if no artwork are available.
     */
    public static String getArtworkUrl(SoundCloudTrack track, String size) {
        String defaultUrl = track.getArtworkUrl();
        if (defaultUrl == null) {
            return null;
        }
        switch (size) {
            case MINI:
            case TINY:
            case SMALL:
            case BADGE:
            case LARGE:
            case XLARGE:
            case XXLARGE:
            case XXXLARGE:
                return defaultUrl.replace(LARGE, size);
            default:
                return defaultUrl;
        }
    }

    /**
     * Retrieve the cover url of a user pointing to the requested size.
     * <p/>
     * By default, {@link SoundCloudUser#getAvatarUrl()}
     * points to the {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#LARGE}
     * <p/>
     * Available size are :
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#MINI}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#TINY}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#SMALL}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#BADGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#LARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XLARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XXLARGE}
     * {@link fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper#XXXLARGE}
     *
     * @param user user for which cover url should be returned.
     * @param size wished size.
     * @return artwork url or null if no artwork are available.
     */
    public static String getCoverUrl(SoundCloudUser user, String size) {
        String defaultUrl = user.getAvatarUrl();
        if (defaultUrl == null) {
            return null;
        }
        switch (size) {
            case MINI:
            case TINY:
            case SMALL:
            case BADGE:
            case LARGE:
            case XLARGE:
            case XXLARGE:
            case XXXLARGE:
                return defaultUrl.replace(LARGE, size);
            default:
                return defaultUrl;
        }
    }

}
