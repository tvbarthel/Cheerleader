package fr.tvbarthel.simplesoundcloud.library;

import org.json.JSONException;
import org.json.JSONObject;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser;
import rx.functions.Func1;

/**
 * Parser for SoundCloud api based on Reactive Java.
 */
final class SimpleSoundCloudRxParser {

    /**
     * Parse {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser} retrieved from
     * SoundCloud API.
     */
    public static final Func1<String, SoundCloudUser> PARSE_USER = new Func1<String, SoundCloudUser>() {
        @Override
        public SoundCloudUser call(String json) {
            SoundCloudUser simpleSoundCloudUser = new SoundCloudUser();

            try {
                JSONObject jsonObject = new JSONObject(json);
                simpleSoundCloudUser.setId(jsonObject.optInt(ID));
                simpleSoundCloudUser.setPermaLink(jsonObject.optString(PERMALINK));
                simpleSoundCloudUser.setPermaLinkUrl(jsonObject.optString(PERMALINK_URL));
                simpleSoundCloudUser.setUserName(jsonObject.optString(USERNAME));
                simpleSoundCloudUser.setUri(jsonObject.optString(URI));
                simpleSoundCloudUser.setAvatarUrl(jsonObject.optString(AVATAR_URL));
                simpleSoundCloudUser.setCountry(jsonObject.optString(COUNTRY));
                simpleSoundCloudUser.setFullName(jsonObject.optString(FULL_NAME));
                simpleSoundCloudUser.setFirstName(jsonObject.optString(FIRST_NAME));
                simpleSoundCloudUser.setLastName(jsonObject.optString(LAST_NAME));
                simpleSoundCloudUser.setCity(jsonObject.optString(CITY));
                simpleSoundCloudUser.setDescription(jsonObject.optString(DESCRIPTION));
                simpleSoundCloudUser.setDiscogsName(jsonObject.optString(DISCOGS_NAME));
                simpleSoundCloudUser.setMyspaceName(jsonObject.optString(MYSPACE_NAME));
                simpleSoundCloudUser.setWebsite(jsonObject.optString(WEBSITE));
                simpleSoundCloudUser.setWebsiteTitle(jsonObject.optString(WEBSITE_TITLE));
                simpleSoundCloudUser.setOnline(jsonObject.optBoolean(ONLINE));
                simpleSoundCloudUser.setTrackCount(jsonObject.optInt(TRACK_COUNT));
                simpleSoundCloudUser.setPlaylistCount(jsonObject.optInt(PLAYLIST_COUNT));
                simpleSoundCloudUser.setPublicFavoritedCount(jsonObject.optInt(PUBLIC_FAVORITE_COUNT));
                simpleSoundCloudUser.setFollowersCount(jsonObject.optInt(FOLLOWERS_COUNT));
                simpleSoundCloudUser.setFollowingsCount(jsonObject.optInt(FOLLOWINGS_COUNT));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return simpleSoundCloudUser;
        }
    };

    /**
     * Log cat.
     */
    private static final String TAG = SimpleSoundCloudRxParser.class.getSimpleName();

    /**
     * FIELD
     */
    private static final String ID = "id";
    private static final String PERMALINK = "permalink";
    private static final String PERMALINK_URL = "permalink_url";
    private static final String USERNAME = "username";
    private static final String URI = "uri";
    private static final String AVATAR_URL = "avatar_url";
    private static final String COUNTRY = "country";
    private static final String FULL_NAME = "full_name";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String CITY = "city";
    private static final String DESCRIPTION = "description";
    private static final String DISCOGS_NAME = "discogs_name";
    private static final String MYSPACE_NAME = "myspace_name";
    private static final String WEBSITE = "website";
    private static final String WEBSITE_TITLE = "website_title";
    private static final String ONLINE = "online";
    private static final String TRACK_COUNT = "track_count";
    private static final String PLAYLIST_COUNT = "playlist_count";
    private static final String PUBLIC_FAVORITE_COUNT = "public_favorite_count";
    private static final String FOLLOWERS_COUNT = "followers_count";
    private static final String FOLLOWINGS_COUNT = "followings_count";

    /**
     * Non instantiable class.
     */
    private SimpleSoundCloudRxParser() {

    }
}
