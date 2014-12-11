package fr.tvbarthel.simplesoundcloud.library.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Encapsulate SoundCloud track data.
 */
public class SoundCloudTrack implements Parcelable {

    /**
     * Parcelable.
     */
    public static final Parcelable.Creator<SoundCloudTrack> CREATOR = new Parcelable.Creator<SoundCloudTrack>() {
        public SoundCloudTrack createFromParcel(Parcel source) {
            return new SoundCloudTrack(source);
        }

        public SoundCloudTrack[] newArray(int size) {
            return new SoundCloudTrack[size];
        }
    };

    private int mId;
    private int mUserId;
    private int mCommentCount;
    private int mDownloadCount;
    private int mPlaybackCount;
    private int mFavoritingCount;
    private int mOriginalContentSize;
    private int mLabelId;
    private int mBmp;
    private long mDurationInMilli;
    private Date mCreationDate;
    private boolean mPublicSharing;
    private boolean mStreamable;
    private boolean mDownloadable;
    private boolean mCommentable;
    private String mPermalink;
    private String mPermalingUrl;
    private String mArtworkUrl;
    private String mDownloadUrl;
    private String mStreamUrl;
    private String mVideoUrl;
    private String mUri;
    private String mPurchaseUrl;
    private String mGenre;
    private String mTitle;
    private String mDescription;
    private String mLabelName;
    private String mTrackType;
    private String mLicense;
    private String mOriginalFormat;
    private String mWaveFormUrl;

    /**
     * Default constructor.
     */
    public SoundCloudTrack() {
    }

    private SoundCloudTrack(Parcel in) {
        this.mId = in.readInt();
        this.mUserId = in.readInt();
        this.mCommentCount = in.readInt();
        this.mDownloadCount = in.readInt();
        this.mPlaybackCount = in.readInt();
        this.mFavoritingCount = in.readInt();
        this.mOriginalContentSize = in.readInt();
        this.mLabelId = in.readInt();
        this.mBmp = in.readInt();
        this.mDurationInMilli = in.readLong();
        long tmpMCreatationDate = in.readLong();
        this.mCreationDate = tmpMCreatationDate == -1 ? null : new Date(tmpMCreatationDate);
        this.mPublicSharing = in.readByte() != 0;
        this.mStreamable = in.readByte() != 0;
        this.mDownloadable = in.readByte() != 0;
        this.mCommentable = in.readByte() != 0;
        this.mPermalink = in.readString();
        this.mPermalingUrl = in.readString();
        this.mArtworkUrl = in.readString();
        this.mDownloadUrl = in.readString();
        this.mStreamUrl = in.readString();
        this.mVideoUrl = in.readString();
        this.mUri = in.readString();
        this.mPurchaseUrl = in.readString();
        this.mGenre = in.readString();
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mLabelName = in.readString();
        this.mTrackType = in.readString();
        this.mLicense = in.readString();
        this.mOriginalFormat = in.readString();
        this.mWaveFormUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeInt(this.mUserId);
        dest.writeInt(this.mCommentCount);
        dest.writeInt(this.mDownloadCount);
        dest.writeInt(this.mPlaybackCount);
        dest.writeInt(this.mFavoritingCount);
        dest.writeInt(this.mOriginalContentSize);
        dest.writeInt(this.mLabelId);
        dest.writeInt(this.mBmp);
        dest.writeLong(this.mDurationInMilli);
        dest.writeLong(mCreationDate != null ? mCreationDate.getTime() : -1);
        dest.writeByte(mPublicSharing ? (byte) 1 : (byte) 0);
        dest.writeByte(mStreamable ? (byte) 1 : (byte) 0);
        dest.writeByte(mDownloadable ? (byte) 1 : (byte) 0);
        dest.writeByte(mCommentable ? (byte) 1 : (byte) 0);
        dest.writeString(this.mPermalink);
        dest.writeString(this.mPermalingUrl);
        dest.writeString(this.mArtworkUrl);
        dest.writeString(this.mDownloadUrl);
        dest.writeString(this.mStreamUrl);
        dest.writeString(this.mVideoUrl);
        dest.writeString(this.mUri);
        dest.writeString(this.mPurchaseUrl);
        dest.writeString(this.mGenre);
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeString(this.mLabelName);
        dest.writeString(this.mTrackType);
        dest.writeString(this.mLicense);
        dest.writeString(this.mOriginalFormat);
        dest.writeString(this.mWaveFormUrl);
    }

    @Override
    public String toString() {
        return "SoundCloudTrack{"
                + "mId=" + mId
                + ", mUserId=" + mUserId
                + ", mCommentCount=" + mCommentCount
                + ", mDownloadCount=" + mDownloadCount
                + ", mPlaybackCount=" + mPlaybackCount
                + ", mFavoritingCount=" + mFavoritingCount
                + ", mOriginalContentSize=" + mOriginalContentSize
                + ", mLabelId=" + mLabelId
                + ", mBmp=" + mBmp
                + ", mDurationInMilli=" + mDurationInMilli
                + ", mCreationDate=" + mCreationDate
                + ", mPublicSharing=" + mPublicSharing
                + ", mStreamable=" + mStreamable
                + ", mDownloadable=" + mDownloadable
                + ", mCommentable=" + mCommentable
                + ", mPermalink='" + mPermalink + '\''
                + ", mPermalingUrl='" + mPermalingUrl + '\''
                + ", mArtworkUrl='" + mArtworkUrl + '\''
                + ", mDownloadUrl='" + mDownloadUrl + '\''
                + ", mStreamUrl='" + mStreamUrl + '\''
                + ", mVideoUrl='" + mVideoUrl + '\''
                + ", mUri='" + mUri + '\''
                + ", mPurchaseUrl='" + mPurchaseUrl + '\''
                + ", mGenre='" + mGenre + '\''
                + ", mTitle='" + mTitle + '\''
                + ", mDescription='" + mDescription + '\''
                + ", mLabelName='" + mLabelName + '\''
                + ", mTrackType='" + mTrackType + '\''
                + ", mLicense='" + mLicense + '\''
                + ", mOriginalFormat='" + mOriginalFormat + '\''
                + ", mWaveUrl='" + mWaveFormUrl + '\''
                + '}';
    }

    /**
     * Integer Id.
     *
     * @return Integer Id.
     */
    public int getId() {
        return mId;
    }

    /**
     * Integer Id.
     *
     * @param id Integer Id.
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * user-id of the owner
     *
     * @return user-id of the owner
     */
    public int getUserId() {
        return mUserId;
    }

    /**
     * user-id of the owner
     *
     * @param userId user-id of the owner
     */
    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    /**
     * track comment count
     *
     * @return track comment count
     */
    public int getCommentCount() {
        return mCommentCount;
    }

    /**
     * track comment count
     *
     * @param commentCount track comment count
     */
    public void setCommentCount(int commentCount) {
        this.mCommentCount = commentCount;
    }

    /**
     * track download count
     *
     * @return track download count
     */
    public int getDownloadCount() {
        return mDownloadCount;
    }

    /**
     * track download count
     *
     * @param downloadCount track download count
     */
    public void setDownloadCount(int downloadCount) {
        this.mDownloadCount = downloadCount;
    }

    /**
     * track play count
     *
     * @return track play count
     */
    public int getPlaybackCount() {
        return mPlaybackCount;
    }

    /**
     * track play count
     *
     * @param playbackCount track play count
     */
    public void setPlaybackCount(int playbackCount) {
        this.mPlaybackCount = playbackCount;
    }

    /**
     * track favoriting count
     *
     * @return track favoriting count
     */
    public int getFavoritingCount() {
        return mFavoritingCount;
    }

    /**
     * track favoriting count
     *
     * @param favoritingCount track favoriting count
     */
    public void setFavoritingCount(int favoritingCount) {
        this.mFavoritingCount = favoritingCount;
    }

    /**
     * size in bytes of the original file
     *
     * @return size in bytes of the original file
     */
    public int getOriginalContentSize() {
        return mOriginalContentSize;
    }

    /**
     * size in bytes of the original file
     *
     * @param originalContentSize size in bytes of the original file
     */
    public void setOriginalContentSize(int originalContentSize) {
        this.mOriginalContentSize = originalContentSize;
    }

    /**
     * id of the label user
     *
     * @return id of the label user
     */
    public int getLabelId() {
        return mLabelId;
    }

    /**
     * id of the label user
     *
     * @param labelId id of the label user
     */
    public void setLabelId(int labelId) {
        this.mLabelId = labelId;
    }

    /**
     * beats per minute
     *
     * @return beats per minute
     */
    public int getBmp() {
        return mBmp;
    }

    /**
     * beats per minute
     *
     * @param bmp beats per minute
     */
    public void setBmp(int bmp) {
        this.mBmp = bmp;
    }

    /**
     * duration in milliseconds
     *
     * @return duration in milliseconds
     */
    public long getDurationInMilli() {
        return mDurationInMilli;
    }

    /**
     * duration in milliseconds
     *
     * @param durationInMilli duration in milliseconds
     */
    public void setDurationInMilli(long durationInMilli) {
        this.mDurationInMilli = durationInMilli;
    }

    /**
     * Date of creation
     *
     * @return Date of creation
     */
    public Date getCreationDate() {
        return mCreationDate;
    }

    /**
     * Date of creation
     *
     * @param creationDate Date of creation
     */
    public void setCreationDate(Date creationDate) {
        this.mCreationDate = creationDate;
    }

    /**
     * Public sharing policy
     *
     * @return true if shared publicly.
     */
    public boolean isPublicSharing() {
        return mPublicSharing;
    }

    /**
     * Public sharing policy
     *
     * @param publicSharing true if shared publicly.
     */
    public void setPublicSharing(boolean publicSharing) {
        this.mPublicSharing = publicSharing;
    }

    /**
     * streamable via API
     *
     * @return true if streamable via API
     */
    public boolean isStreamable() {
        return mStreamable;
    }

    /**
     * streamable via API
     *
     * @param streamable true if streamable via API
     */
    public void setStreamable(boolean streamable) {
        this.mStreamable = streamable;
    }

    /**
     * downloadable via API
     *
     * @return true if downloadable via API
     */
    public boolean isDownloadable() {
        return mDownloadable;
    }

    /**
     * downloadable via API
     *
     * @param downloadable true if downloadable via API
     */
    public void setDownloadable(boolean downloadable) {
        this.mDownloadable = downloadable;
    }

    /**
     * track commentable
     *
     * @return true if track commentable
     */
    public boolean isCommentable() {
        return mCommentable;
    }

    /**
     * track commentable
     *
     * @param commentable true if track commentable
     */
    public void setCommentable(boolean commentable) {
        this.mCommentable = commentable;
    }

    /**
     * permalink of the resource
     *
     * @return permalink of the resource
     */
    public String getPermalink() {
        return mPermalink;
    }

    /**
     * permalink of the resource
     *
     * @param permalink permalink of the resource
     */
    public void setPermalink(String permalink) {
        this.mPermalink = permalink;
    }

    /**
     * URL to the SoundCloud.com page
     *
     * @return URL to the SoundCloud.com page
     */
    public String getPermalingUrl() {
        return mPermalingUrl;
    }

    /**
     * URL to the SoundCloud.com page
     *
     * @param permalingUrl URL to the SoundCloud.com page
     */
    public void setPermalingUrl(String permalingUrl) {
        this.mPermalingUrl = permalingUrl;
    }

    /**
     * URL to a JPEG image
     *
     * @return URL to a JPEG image
     */
    public String getArtworkUrl() {
        return mArtworkUrl;
    }

    /**
     * URL to a JPEG image
     *
     * @param artworkUrl URL to a JPEG image
     */
    public void setArtworkUrl(String artworkUrl) {
        this.mArtworkUrl = artworkUrl;
    }

    /**
     * URL to original file
     *
     * @return URL to original file
     */
    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    /**
     * URL to original file
     *
     * @param downloadUrl URL to original file
     */
    public void setDownloadUrl(String downloadUrl) {
        this.mDownloadUrl = downloadUrl;
    }

    /**
     * link to 128kbs mp3 stream
     *
     * @return link to 128kbs mp3 stream
     */
    public String getStreamUrl() {
        return mStreamUrl;
    }

    /**
     * link to 128kbs mp3 stream
     *
     * @param streamUrl link to 128kbs mp3 stream
     */
    public void setStreamUrl(String streamUrl) {
        this.mStreamUrl = streamUrl;
    }

    /**
     * a link to a video page
     *
     * @return a link to a video page
     */
    public String getVideoUrl() {
        return mVideoUrl;
    }

    /**
     * a link to a video page
     *
     * @param videoUrl a link to a video page
     */
    public void setVideoUrl(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    /**
     * API resource URL
     *
     * @return API resource URL
     */
    public String getUri() {
        return mUri;
    }

    /**
     * API resource URL
     *
     * @param uri API resource URL
     */
    public void setUri(String uri) {
        this.mUri = uri;
    }

    /**
     * external purchase link
     *
     * @return external purchase link
     */
    public String getPurchaseUrl() {
        return mPurchaseUrl;
    }

    /**
     * external purchase link
     *
     * @param purchaseUrl external purchase link
     */
    public void setPurchaseUrl(String purchaseUrl) {
        this.mPurchaseUrl = purchaseUrl;
    }

    /**
     * genre
     * <p/>
     * example : "HipHop"
     *
     * @return genre
     */
    public String getGenre() {
        return mGenre;
    }

    /**
     * genre
     * <p/>
     * example : "HipHop"
     *
     * @param genre genre
     */
    public void setGenre(String genre) {
        this.mGenre = genre;
    }

    /**
     * track title
     *
     * @return track title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * track title
     *
     * @param title track title
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * HTML description
     *
     * @return HTML description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * HTML description
     *
     * @param description HTML description
     */
    public void setDescription(String description) {
        this.mDescription = description;
    }

    /**
     * label name
     * <p/>
     * Example : "BeatLabel"
     *
     * @return label name
     */
    public String getLabelName() {
        return mLabelName;
    }

    /**
     * label name
     * <p/>
     * Example : "BeatLabel"
     *
     * @param labelName label name
     */
    public void setLabelName(String labelName) {
        this.mLabelName = labelName;
    }

    /**
     * track type
     * <p/>
     * Example : "live"
     *
     * @return track type
     */
    public String getTrackType() {
        return mTrackType;
    }

    /**
     * track type
     * <p/>
     * Example : "live"
     *
     * @param trackType track type
     */
    public void setTrackType(String trackType) {
        this.mTrackType = trackType;
    }

    /**
     * creative common license
     *
     * @return creative common license
     */
    public String getLicense() {
        return mLicense;
    }

    /**
     * creative common license
     *
     * @param license creative common license
     */
    public void setLicense(String license) {
        this.mLicense = license;
    }

    /**
     * file format of the original file
     *
     * @return file format of the original file
     */
    public String getOriginalFormat() {
        return mOriginalFormat;
    }

    /**
     * file format of the original file
     *
     * @param originalFormat file format of the original file
     */
    public void setOriginalFormat(String originalFormat) {
        this.mOriginalFormat = originalFormat;
    }

    /**
     * URL to PNG waveform image
     * @return URL to PNG waveform image
     */
    public String getWaveFormUrl() {
        return mWaveFormUrl;
    }

    /**
     * URL to PNG waveform image
     * @param waveFormUrl URL to PNG waveform image
     */
    public void setWaveFormUrl(String waveFormUrl) {
        this.mWaveFormUrl = waveFormUrl;
    }
}
