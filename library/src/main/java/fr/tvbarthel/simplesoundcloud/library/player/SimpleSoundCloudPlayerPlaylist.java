package fr.tvbarthel.simplesoundcloud.library.player;

import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudPlaylist;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Used to manage the player playlist.
 */
public final class SimpleSoundCloudPlayerPlaylist {

    /**
     * Singleton pattern.
     */
    private static SimpleSoundCloudPlayerPlaylist sInstance;

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
    private SimpleSoundCloudPlayerPlaylist() {
        mSoundCloudPlaylist = new SoundCloudPlaylist();
        mCurrentTrackIndex = -1;
    }

    /**
     * Retrieve the instance of the playlist manager.
     *
     * @return instance.
     */
    public static SimpleSoundCloudPlayerPlaylist getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleSoundCloudPlayerPlaylist();
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
     * Return the current track.
     *
     * @return current track or null if none has been added to the player playlist.
     */
    public SoundCloudTrack getCurrentTrack() {
        if (mCurrentTrackIndex > -1) {
            return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
        }
        return null;
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
     * Add all tracks at the end of the playlist.
     *
     * @param tracks tracks to add.
     */
    public void addAll(List<SoundCloudTrack> tracks) {
        for (SoundCloudTrack track : tracks) {
            add(mSoundCloudPlaylist.getTracks().size(), track);
        }
    }

    /**
     * Add a track to the given position.
     *
     * @param position position of the track to insert
     * @param track    track to insert.
     */
    public void add(int position, SoundCloudTrack track) {
        if (mCurrentTrackIndex == -1) {
            mCurrentTrackIndex = 0;
        }
        mSoundCloudPlaylist.addTrack(position, track);
    }

    /**
     * Remove a track from the SoundCloud player playlist.
     * <p/>
     * If the track is currently played, it will be stopped before being removed.
     *
     * @param trackIndex index of the track to be removed.
     * @return track removed or null if given index can't be found.
     */
    public SoundCloudTrack remove(int trackIndex) {

        SoundCloudTrack removedTrack = null;
        ArrayList<SoundCloudTrack> tracks = mSoundCloudPlaylist.getTracks();

        // check if track is in the playlist
        if (trackIndex >= 0 && trackIndex < tracks.size()) {
            removedTrack = tracks.remove(trackIndex);

            // update the current index
            if (tracks.size() == 0) {
                // last song has been removed
                mCurrentTrackIndex = 0;
            } else if (mCurrentTrackIndex == trackIndex) {
                // update current index and start the nex track if player wasn't paused.
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % tracks.size();
            } else if (mCurrentTrackIndex > trackIndex) {
                // update current track index if the removed one was before
                // in the playlist
                mCurrentTrackIndex = (mCurrentTrackIndex - 1) % tracks.size();
            }

        }


        return removedTrack;
    }

    /**
     * Retrieve the next track.
     *
     * @return next track to be played.
     */
    public SoundCloudTrack next() {
        mCurrentTrackIndex = (mCurrentTrackIndex + 1) % mSoundCloudPlaylist.getTracks().size();
        return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
    }

    /**
     * Retrieve the previous track.
     *
     * @return previous track to be played.
     */
    public SoundCloudTrack previous() {
        int tracks = mSoundCloudPlaylist.getTracks().size();
        mCurrentTrackIndex = (tracks + mCurrentTrackIndex - 1) % tracks;
        return mSoundCloudPlaylist.getTracks().get(mCurrentTrackIndex);
    }
}
