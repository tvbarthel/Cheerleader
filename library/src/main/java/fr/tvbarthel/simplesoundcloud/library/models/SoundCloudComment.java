package fr.tvbarthel.simplesoundcloud.library.models;

import java.util.Date;

/**
 * A SoundCloud comment written by a SoundCloud user related to a SoundCloud track.
 */
public class SoundCloudComment {
    private int mId;
    private Date mCreationDate;
    private int mTrackId;
    private int mTrackTimeStamp;
    private int mContent;
    private int mUserId;
    private int mUserName;
    private int mUserAvatarUrl;

    /**
     * Id used to identify the comment.
     *
     * @return Id used to identify the comment.
     */
    public int getId() {
        return mId;
    }

    /**
     * Id used to identify the comment.
     *
     * @param id used to identify the comment.
     */
    public void setId(int id) {
        this.mId = id;
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
     * Comment creation date.
     *
     * @param creationDate date of the creation of the comment.
     */
    public void setCreationDate(Date creationDate) {
        this.mCreationDate = creationDate;
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
     * The track id of the related track.
     *
     * @param trackId The track id of the related track.
     */
    public void setTrackId(int trackId) {
        this.mTrackId = trackId;
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
     * Associated position in the track.
     * <p/>
     * For instance 10000, user made his comment at 10sec
     *
     * @param trackTimeStamp playback milliseconds at which user made the comment.
     */
    public void setTrackTimeStamp(int trackTimeStamp) {
        this.mTrackTimeStamp = trackTimeStamp;
    }

    /**
     * Content of the comment.
     *
     * @return comment message.
     */
    public int getContent() {
        return mContent;
    }

    /**
     * Content of the comment.
     *
     * @param content comment message.
     */
    public void setContent(int content) {
        this.mContent = content;
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
     * User id of the user who made the comment
     *
     * @param userId SoundCloud user id.
     */
    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    /**
     * Name of the user who made the comment.
     *
     * @return name of the user who made the comment.
     */
    public int getUserName() {
        return mUserName;
    }

    /**
     * Name of the user who made the comment.
     *
     * @param userName name of the user who made the comment.
     */
    public void setUserName(int userName) {
        this.mUserName = userName;
    }

    /**
     * Url of the user's avatar.
     *
     * @return url of the avatar.
     */
    public int getUserAvatarUrl() {
        return mUserAvatarUrl;
    }

    /**
     * Url of the user's avatar.
     *
     * @param userAvatarUrl url of the avatar.
     */
    public void setUserAvatarUrl(int userAvatarUrl) {
        this.mUserAvatarUrl = userAvatarUrl;
    }
}
