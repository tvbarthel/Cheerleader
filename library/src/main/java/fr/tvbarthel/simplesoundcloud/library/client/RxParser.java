package fr.tvbarthel.simplesoundcloud.library.client;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudUser;
import rx.functions.Func1;

/**
 * Parser for SoundCloud api based on Reactive Java.
 */
final class RxParser {

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
                Log.e(TAG, "FAILED TO PARSE_USER : " + json);
            }

            return simpleSoundCloudUser;
        }
    };

    /**
     * Parse all public {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack}
     * of a user.
     */
    public static final Func1<String, ArrayList<SoundCloudTrack>> PARSE_USER_TRACKS
            = new Func1<String, ArrayList<SoundCloudTrack>>() {
        @Override
        public ArrayList<SoundCloudTrack> call(String s) {
            ArrayList<SoundCloudTrack> tracks = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    tracks.add(PARSE_TRACK.call(jsonArray.getJSONObject(i).toString()));
                }

            } catch (JSONException e) {
                Log.e(TAG, "FAILED TO PARSE USER TRACKS : " + s);
            }
            return tracks;
        }
    };

    /**
     * Parse {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack} retrieved from
     * SoundCloud API.
     */
    public static final Func1<String, SoundCloudTrack> PARSE_TRACK = new Func1<String, SoundCloudTrack>() {
        @Override
        public SoundCloudTrack call(String json) {
            SoundCloudTrack track = new SoundCloudTrack();
            try {
                JSONObject jsonObject = new JSONObject(json);
                track.setId(jsonObject.optInt(ID));
                track.setUserId(jsonObject.optInt(USER_ID));
                track.setUserId(jsonObject.optInt(USER_ID));
                track.setCommentCount(jsonObject.optInt(COMNENT_COUNT));
                track.setFavoritingCount(jsonObject.optInt(FAVORITINGS_COUNT));
                track.setPlaybackCount(jsonObject.optInt(PLAYBACK_COUNT));
                track.setDownloadCount(jsonObject.optInt(DOWNLOAD_COUNT));
                track.setOriginalContentSize(jsonObject.optInt(ORIGINAL_CONTENT_SIZE));
                track.setLabelId(jsonObject.optInt(LABEL_ID));
                track.setBmp(jsonObject.optInt(BMP));
                track.setDurationInMilli(jsonObject.optLong(DURATION));

                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
                String createdAt = jsonObject.optString(CREATED_AT);
                if (createdAt != null) {
                    track.setCreationDate(format.parse(createdAt));
                }

                String title = jsonObject.optString(TITLE);
                int dashIndex = title.indexOf('-');
                if (dashIndex == -1) {
                    track.setTitle(title);
                } else {
                    track.setTitle(title.substring(dashIndex + 1, title.length()).trim());
                    track.setArtist(title.substring(0, dashIndex).trim());
                }

                track.setStreamable(jsonObject.optBoolean(STREAMABLE));
                track.setDownloadable(jsonObject.optBoolean(DOWNLOADABLE));
                track.setCommentable(jsonObject.optBoolean(COMMENTABLE));
                track.setPermalingUrl(jsonObject.optString(PERMALINK_URL));
                track.setPermalink(jsonObject.optString(PERMALINK));
                track.setArtworkUrl(jsonObject.optString(ARTWORK_URL));
                track.setWaveFormUrl(jsonObject.optString(WAVEFORM_URL));
                track.setDownloadUrl(jsonObject.optString(DOWNLOAD_URL));
                track.setStreamUrl(jsonObject.optString(STREAM_URL));
                track.setVideoUrl(jsonObject.optString(VIDEO_URL));
                track.setPurchaseUrl(jsonObject.optString(PURCHASE_URL));
                track.setUri(jsonObject.optString(URI));
                track.setGenre(jsonObject.optString(GENRE));
                track.setDescription(jsonObject.optString(DESCRIPTION));
                track.setLabelName(jsonObject.optString(LABEL_NAME));
                track.setTrackType(jsonObject.optString(TRACK_TYPE));
                track.setLicense(jsonObject.optString(LICENCE));
                track.setOriginalFormat(jsonObject.optString(ORIGINAL_FORMAT));
                String sharingPublic = jsonObject.optString(SHARING);
                if (sharingPublic == null || !sharingPublic.endsWith(PUBLIC)) {
                    track.setPublicSharing(false);
                } else {
                    track.setPublicSharing(true);
                }


            } catch (JSONException e) {
                Log.e(TAG, "FAILED TO PARSE_USER : " + json);
            } catch (ParseException e) {
                Log.e(TAG, "FAILED TO PARSE_USER, date parsing error : " + json);
            }
            return track;
        }
    };

    /**
     * Log cat.
     */
    private static final String TAG = RxParser.class.getSimpleName();

    /**
     * FIELD
     */
    private static final String ID = "id";
    private static final String USER_ID = "user_id";
    private static final String PERMALINK = "permalink";
    private static final String PERMALINK_URL = "permalink_url";
    private static final String ARTWORK_URL = "artwork_url";
    private static final String WAVEFORM_URL = "waveform_url";
    private static final String DOWNLOAD_URL = "download_url";
    private static final String STREAM_URL = "stream_url";
    private static final String VIDEO_URL = "video_url";
    private static final String PURCHASE_URL = "purchase_url";
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
    private static final String COMNENT_COUNT = "comment_count";
    private static final String FAVORITINGS_COUNT = "favoritings_count";
    private static final String DOWNLOAD_COUNT = "download_count";
    private static final String PLAYBACK_COUNT = "playback_count";
    private static final String ORIGINAL_CONTENT_SIZE = "original_content_size";
    private static final String LABEL_ID = "label_id";
    private static final String BMP = "bmp";
    private static final String DURATION = "duration";
    private static final String CREATED_AT = "created_at";
    private static final String STREAMABLE = "streamable";
    private static final String DOWNLOADABLE = "downloadable";
    private static final String COMMENTABLE = "commentable";
    private static final String GENRE = "genre";
    private static final String TITLE = "title";
    private static final String LABEL_NAME = "label_name";
    private static final String TRACK_TYPE = "track_type";
    private static final String LICENCE = "license";
    private static final String ORIGINAL_FORMAT = "original_format";
    private static final String PUBLIC = "public";
    private static final String SHARING = "sharing";

    /**
     * Non instantiable class.
     */
    private RxParser() {

    }
}
