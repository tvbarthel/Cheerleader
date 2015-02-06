package fr.tvbarthel.simplesoundcloud.library.player;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudPlaylist;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Used to manage the player playlist.
 */
public final class SimpleSoundCloudPlaylistManager {

    /**
     * Singleton pattern.
     */
    private static SimpleSoundCloudPlaylistManager sInstance;

    /**
     * Current playlist.
     */
    private SoundCloudPlaylist mSoundCloudPlaylist;

    /**
     * Index of the current track.
     */
    private int mCurrentTrackIndex;

    /**
     * Singleton.
     */
    private SimpleSoundCloudPlaylistManager() {
        mSoundCloudPlaylist = new SoundCloudPlaylist();
    }

    /**
     * Retrieve the instance of the playlist manager.
     *
     * @return instance.
     */
    public static SimpleSoundCloudPlaylistManager getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleSoundCloudPlaylistManager();
        }
        return sInstance;
    }

    /**
     * Retrieve the current playlist.
     *
     * @return current playlist.
     */
    public SoundCloudPlaylist getPlaylist() {
        return mSoundCloudPlaylist;
    }

    /**
     * Add a track at the end of the playlist.
     *
     * @param track track to be added.
     */
    public void add(SoundCloudTrack track) {
        add(mSoundCloudPlaylist.getTracks().size(), track);
    }

    /**
     * Add a track to the given position.
     *
     * @param position position of the track to insert
     * @param track    track to insert.
     */
    public void add(int position, SoundCloudTrack track) {
        mSoundCloudPlaylist.addTrack(position, track);
    }

    /**
     * Remove a track from the SoundCloud player playlist.
     * <p/>
     * If the track is currently played, it will be stopped before being removed.
     *
     * @param playlistIndex index of the track to be removed.
     */
    public void remove(int playlistIndex) {

    }

    /**
     * Retrieve the next track.
     *
     * @return next track to be played.
     */
    public SoundCloudTrack next() {
        return new SoundCloudTrack();
    }

    /**
     * Retrieve the previous track.
     *
     * @return previous track to be played.
     */
    public SoundCloudTrack previous() {
        return new SoundCloudTrack();
    }
}
