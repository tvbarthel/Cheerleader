package fr.tvbarthel.simplesoundcloud.library.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Encapsulate SoundCloudUser data.
 */
public class SoundCloudUser implements Parcelable {

    /**
     * Parcelable.
     */
    public static final Parcelable.Creator<SoundCloudUser> CREATOR
            = new Parcelable.Creator<SoundCloudUser>() {
        public SoundCloudUser createFromParcel(Parcel source) {
            return new SoundCloudUser(source);
        }

        public SoundCloudUser[] newArray(int size) {
            return new SoundCloudUser[size];
        }
    };

    private int mId;
    private String mPermaLink;
    private String mUserName;
    private String mUri;
    private String mPermaLinkUrl;
    private String mAvatarUrl;
    private String mCountry;
    private String mFullName;
    private String mFirstName;
    private String mLastName;
    private String mCity;
    private String mDescription;
    private String mDiscogsName;
    private String mMyspaceName;
    private String mWebsite;
    private String mWebsiteTitle;
    private boolean mOnline;
    private int mTrackCount;
    private int mPlaylistCount;
    private int mFollowersCount;
    private int mFollowingsCount;
    private int mPublicFavoritedCount;

    /**
     * Default constructor.
     */
    public SoundCloudUser() {
    }

    /**
     * Parcelable.
     *
     * @param in parcel.
     */
    private SoundCloudUser(Parcel in) {
        this.mId = in.readInt();
        this.mPermaLink = in.readString();
        this.mUserName = in.readString();
        this.mUri = in.readString();
        this.mPermaLinkUrl = in.readString();
        this.mAvatarUrl = in.readString();
        this.mCountry = in.readString();
        this.mFullName = in.readString();
        this.mFirstName = in.readString();
        this.mLastName = in.readString();
        this.mCity = in.readString();
        this.mDescription = in.readString();
        this.mDiscogsName = in.readString();
        this.mMyspaceName = in.readString();
        this.mWebsite = in.readString();
        this.mWebsiteTitle = in.readString();
        this.mOnline = in.readByte() != 0;
        this.mTrackCount = in.readInt();
        this.mPlaylistCount = in.readInt();
        this.mFollowersCount = in.readInt();
        this.mFollowingsCount = in.readInt();
        this.mPublicFavoritedCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mPermaLink);
        dest.writeString(this.mUserName);
        dest.writeString(this.mUri);
        dest.writeString(this.mPermaLinkUrl);
        dest.writeString(this.mAvatarUrl);
        dest.writeString(this.mCountry);
        dest.writeString(this.mFullName);
        dest.writeString(this.mFirstName);
        dest.writeString(this.mLastName);
        dest.writeString(this.mCity);
        dest.writeString(this.mDescription);
        dest.writeString(this.mDiscogsName);
        dest.writeString(this.mMyspaceName);
        dest.writeString(this.mWebsite);
        dest.writeString(this.mWebsiteTitle);
        dest.writeByte(mOnline ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mTrackCount);
        dest.writeInt(this.mPlaylistCount);
        dest.writeInt(this.mFollowersCount);
        dest.writeInt(this.mFollowingsCount);
        dest.writeInt(this.mPublicFavoritedCount);
    }

    @Override
    public String toString() {
        return "SoundCloudUser{"
                + "mId=" + mId
                + ", mPermaLink='" + mPermaLink + '\''
                + ", mUserName='" + mUserName + '\''
                + ", mUri='" + mUri + '\''
                + ", mPermaLinkUrl='" + mPermaLinkUrl + '\''
                + ", mAvatarUrl='" + mAvatarUrl + '\''
                + ", mCountry='" + mCountry + '\''
                + ", mFullName='" + mFullName + '\''
                + ", mFirstName='" + mFirstName + '\''
                + ", mLastName='" + mLastName + '\''
                + ", mCity='" + mCity + '\''
                + ", mDescription='" + mDescription + '\''
                + ", mDiscogsName='" + mDiscogsName + '\''
                + ", mMyspaceName='" + mMyspaceName + '\''
                + ", mWebsite='" + mWebsite + '\''
                + ", mWebsiteTitle='" + mWebsiteTitle + '\''
                + ", mOnline=" + mOnline
                + ", mTrackCount=" + mTrackCount
                + ", mPlaylistCount=" + mPlaylistCount
                + ", mFollowersCount=" + mFollowersCount
                + ", mFollowingsCount=" + mFollowingsCount
                + ", mPublicFavoritedCount=" + mPublicFavoritedCount
                + '}';
    }

    /**
     * User id.
     *
     * @return user id.
     */
    public int getId() {
        return mId;
    }

    /**
     * User id.
     *
     * @param id user id.
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * Permalink of the resource.
     *
     * @return Permalink of the resource.
     */
    public String getPermaLink() {
        return mPermaLink;
    }

    /**
     * Permalink of the resource.
     *
     * @param permaLink Permalink of the resource.
     */
    public void setPermaLink(String permaLink) {
        this.mPermaLink = permaLink;
    }

    /**
     * Username.
     *
     * @return username.
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * Username.
     *
     * @param userName Username.
     */
    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    /**
     * API resource.
     * <p/>
     * Example : URL http://api.soundcloud.com/comments/32562
     *
     * @return API resource.
     */
    public String getUri() {
        return mUri;
    }

    /**
     * API resource.
     * <p/>
     * Example : URL http://api.soundcloud.com/comments/32562
     *
     * @param uri API resource.
     */
    public void setUri(String uri) {
        this.mUri = uri;
    }

    /**
     * URL to the SoundCloud.com page.
     * <p/>
     * Example : "http://soundcloud.com/bryan/sbahn-sounds"
     *
     * @return URL to the SoundCloud.com page.
     */
    public String getPermaLinkUrl() {
        return mPermaLinkUrl;
    }

    /**
     * URL to the SoundCloud.com page.
     * <p/>
     * Example : "http://soundcloud.com/bryan/sbahn-sounds"
     *
     * @param permaLinkUrl URL to the SoundCloud.com page.
     */
    public void setPermaLinkUrl(String permaLinkUrl) {
        this.mPermaLinkUrl = permaLinkUrl;
    }

    /**
     * URL to a JPEG image.
     * <p/>
     * Example : "http://i1.sndcdn.com/avatars-000011353294-n0axp1-large.jpg"
     *
     * @return URL to a JPEG image.
     */
    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    /**
     * URL to a JPEG image.
     * <p/>
     * Example : "http://i1.sndcdn.com/avatars-000011353294-n0axp1-large.jpg"
     *
     * @param avatarUrl URL to a JPEG image.
     */
    public void setAvatarUrl(String avatarUrl) {
        this.mAvatarUrl = avatarUrl;
    }

    /**
     * Country.
     *
     * @return country.
     */
    public String getCountry() {
        return mCountry;
    }

    /**
     * Country.
     *
     * @param country country.
     */
    public void setCountry(String country) {
        this.mCountry = country;
    }

    /**
     * First and last name.
     * <p/>
     * Example : "Tom Wilson"
     *
     * @return First and last name separate by an empty space.
     */
    public String getFullName() {
        return mFullName;
    }

    /**
     * First and last name.
     * <p/>
     * Example : "Tom Wilson"
     *
     * @param fullName First and last name separate by an empty space.
     */
    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    /**
     * First name.
     *
     * @return first name.
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * First name.
     *
     * @param firstName first name.
     */
    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    /**
     * Last name.
     *
     * @return last name.
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * Last name.
     *
     * @param lastName last name.
     */
    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    /**
     * City.
     *
     * @return city.
     */
    public String getCity() {
        return mCity;
    }

    /**
     * City.
     *
     * @param city city.
     */
    public void setCity(String city) {
        this.mCity = city;
    }

    /**
     * Description.
     * Example : "Buskers playing in the S-Bahn station in Berlin"
     *
     * @return Description.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Description.
     * Example : "Buskers playing in the S-Bahn station in Berlin"
     *
     * @param description Description.
     */
    public void setDescription(String description) {
        this.mDescription = description;
    }

    /**
     * Discogs name.
     * Example : "myrandomband"
     *
     * @return Discogs name.
     */
    public String getDiscogsName() {
        return mDiscogsName;
    }

    /**
     * Discogs name.
     * Example : "myrandomband"
     *
     * @param discogsName Discogs name.
     */
    public void setDiscogsName(String discogsName) {
        this.mDiscogsName = discogsName;
    }

    /**
     * MySpace name.
     * <p/>
     * Example : "myrandomband"
     *
     * @return MySpace name.
     */
    public String getMyspaceName() {
        return mMyspaceName;
    }

    /**
     * MySpace name.
     * <p/>
     * Example : "myrandomband"
     *
     * @param myspaceName MySpace name.
     */
    public void setMyspaceName(String myspaceName) {
        this.mMyspaceName = myspaceName;
    }

    /**
     * a URL to the website.
     * <p/>
     * Example : "http://facebook.com/myrandomband"
     *
     * @return a URL to the website.
     */
    public String getWebsite() {
        return mWebsite;
    }

    /**
     * a URL to the website.
     * <p/>
     * Example : "http://facebook.com/myrandomband"
     *
     * @param website a URL to the website.
     */
    public void setWebsite(String website) {
        this.mWebsite = website;
    }

    /**
     * a custom title for the mWebsite.
     * Ewample : "myrandomband on Facebook"
     *
     * @return a custom title for the mWebsite.
     */
    public String getWebsiteTitle() {
        return mWebsiteTitle;
    }

    /**
     * a custom title for the mWebsite.
     * Ewample : "myrandomband on Facebook"
     *
     * @param websiteTitle a custom title for the mWebsite.
     */
    public void setWebsiteTitle(String websiteTitle) {
        this.mWebsiteTitle = websiteTitle;
    }

    /**
     * mOnline status.
     *
     * @return true of user is online.
     */
    public boolean isOnline() {
        return mOnline;
    }

    /**
     * mOnline status.
     *
     * @param online true of user is online.
     */
    public void setOnline(boolean online) {
        this.mOnline = online;
    }

    /**
     * Number of public tracks.
     *
     * @return Number of public tracks.
     */
    public int getTrackCount() {
        return mTrackCount;
    }

    /**
     * Number of public tracks.
     *
     * @param trackCount Number of public tracks.
     */
    public void setTrackCount(int trackCount) {
        this.mTrackCount = trackCount;
    }

    /**
     * Number of public playlists.
     *
     * @return Number of public playlists.
     */
    public int getPlaylistCount() {
        return mPlaylistCount;
    }

    /**
     * Number of public playlists.
     *
     * @param playlistCount Number of public playlists.
     */
    public void setPlaylistCount(int playlistCount) {
        this.mPlaylistCount = playlistCount;
    }

    /**
     * Number of followers.
     *
     * @return Number of followers.
     */
    public int getFollowersCount() {
        return mFollowersCount;
    }

    /**
     * Number of followers.
     *
     * @param followersCount Number of followers.
     */
    public void setFollowersCount(int followersCount) {
        this.mFollowersCount = followersCount;
    }

    /**
     * Number of followed users.
     *
     * @return Number of followed users.
     */
    public int getFollowingsCount() {
        return mFollowingsCount;
    }

    /**
     * Number of followed users.
     *
     * @param followingsCount Number of followed users.
     */
    public void setFollowingsCount(int followingsCount) {
        this.mFollowingsCount = followingsCount;
    }

    /**
     * number of favorited public tracks
     *
     * @return number of favorited public tracks
     */
    public int getPublicFavoritedCount() {
        return mPublicFavoritedCount;
    }

    /**
     * number of favorited public tracks
     *
     * @param publicFavoritedCount number of favorited public tracks
     */
    public void setPublicFavoritedCount(int publicFavoritedCount) {
        this.mPublicFavoritedCount = publicFavoritedCount;
    }

}
