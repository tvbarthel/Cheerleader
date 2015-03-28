package fr.tvbarthel.simplesoundcloud.library.client;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * A SoundCloud comment written by a SoundCloud user related to a SoundCloud track.
 * <p/>
 * <p/>
 * https://developers.soundcloud.com/docs/api/reference#comments
 * Comments can be made on tracks by any user who has access to a track, except for non
 * commentable tracks. As you see in the SoundCloud player comments can also be associated
 * with a specific timestamp in a track.
 */
public class SoundCloudComment implements Parcelable {

    /**
     * Parcelable.
     */
    public static final Parcelable.Creator<SoundCloudComment> CREATOR
            = new Parcelable.Creator<SoundCloudComment>() {
        public SoundCloudComment createFromParcel(Parcel source) {
            return new SoundCloudComment(source);
        }

        public SoundCloudComment[] newArray(int size) {
            return new SoundCloudComment[size];
        }
    };

    private int mId;
    private Date mCreationDate;
    private int mTrackId;
    private int mTrackTimeStamp;
    private String mContent;
    private int mUserId;
    private String mUserName;
    private String mUserAvatarUrl;

    /**
     * Default constructor.
     */
    public SoundCloudComment() {
    }

    private SoundCloudComment(Parcel in) {
        this.mId = in.readInt();
        long tmpMCreationDate = in.readLong();
        this.mCreationDate = tmpMCreationDate == -1 ? null : new Date(tmpMCreationDate);
        this.mTrackId = in.readInt();
        this.mTrackTimeStamp = in.readInt();
        this.mContent = in.readString();
        this.mUserId = in.readInt();
        this.mUserName = in.readString();
        this.mUserAvatarUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeLong(mCreationDate != null ? mCreationDate.getTime() : -1);
        dest.writeInt(this.mTrackId);
        dest.writeInt(this.mTrackTimeStamp);
        dest.writeString(this.mContent);
        dest.writeInt(this.mUserId);
        dest.writeString(this.mUserName);
        dest.writeString(this.mUserAvatarUrl);
    }

    @Override
    public String toString() {
        return "SoundCloudComment{"
                + "mId=" + mId
                + ", mCreationDate=" + mCreationDate
                + ", mTrackId=" + mTrackId
                + ", mTrackTimeStamp=" + mTrackTimeStamp
                + ", mContent=" + mContent
                + ", mUserId=" + mUserId
                + ", mUserName=" + mUserName
                + ", mUserAvatarUrl=" + mUserAvatarUrl
                + '}';
    }

    /**
     * Id used to identify the comment.
     *
     * @return Id used to identify the comment.
     */
    public int getId() {
        return mId;
    }

    /**
     * Comment creation date.
     *
     * @return date of the creation of the comment.
     */
    public Date getCreationDate() {
        return mCreationDate;
    }

    /**
     * The track id of the related track.
     *
     * @return The track id of the related track.
     */
    public int getTrackId() {
        return mTrackId;
    }

    /**
     * Associated position in the track.
     * <p/>
     * For instance 10000, user made his comment at 10sec
     *
     * @return playback milliseconds at which user made the comment.
     */
    public int getTrackTimeStamp() {
        return mTrackTimeStamp;
    }

    /**
     * Content of the comment.
     *
     * @return comment message.
     */
    public String getContent() {
        return mContent;
    }

    /**
     * User id of the user who made the comment
     *
     * @return SoundCloud user id.
     */
    public int getUserId() {
        return mUserId;
    }

    /**
     * Name of the user who made the comment.
     *
     * @return name of the user who made the comment.
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * Url of the user's avatar.
     *
     * @return url of the avatar.
     */
    public String getUserAvatarUrl() {
        return mUserAvatarUrl;
    }

    /**
     * Id used to identify the comment.
     *
     * @param id used to identify the comment.
     */
    void setId(int id) {
        this.mId = id;
    }

    /**
     * Comment creation date.
     *
     * @param creationDate date of the creation of the comment.
     */
    void setCreationDate(Date creationDate) {
        this.mCreationDate = creationDate;
    }

    /**
     * The track id of the related track.
     *
     * @param trackId The track id of the related track.
     */
    void setTrackId(int trackId) {
        this.mTrackId = trackId;
    }

    /**
     * Associated position in the track.
     * <p/>
     * For instance 10000, user made his comment at 10sec
     *
     * @param trackTimeStamp playback milliseconds at which user made the comment.
     */
    void setTrackTimeStamp(int trackTimeStamp) {
        this.mTrackTimeStamp = trackTimeStamp;
    }

    /**
     * Content of the comment.
     *
     * @param content comment message.
     */
    void setContent(String content) {
        this.mContent = content;
    }

    /**
     * User id of the user who made the comment
     *
     * @param userId SoundCloud user id.
     */
    void setUserId(int userId) {
        this.mUserId = userId;
    }

    /**
     * Name of the user who made the comment.
     *
     * @param userName name of the user who made the comment.
     */
    void setUserName(String userName) {
        this.mUserName = userName;
    }

    /**
     * Url of the user's avatar.
     *
     * @param userAvatarUrl url of the avatar.
     */
    void setUserAvatarUrl(String userAvatarUrl) {
        this.mUserAvatarUrl = userAvatarUrl;
    }
}
