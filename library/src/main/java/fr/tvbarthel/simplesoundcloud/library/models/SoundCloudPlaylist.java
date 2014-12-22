package fr.tvbarthel.simplesoundcloud.library.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulate data of a SoundCloud playlist.
 */
public class SoundCloudPlaylist implements Parcelable {

    /**
     * Parcelable.
     */
    public static final Parcelable.Creator<SoundCloudPlaylist> CREATOR
            = new Parcelable.Creator<SoundCloudPlaylist>() {
        public SoundCloudPlaylist createFromParcel(Parcel source) {
            return new SoundCloudPlaylist(source);
        }

        public SoundCloudPlaylist[] newArray(int size) {
            return new SoundCloudPlaylist[size];
        }
    };

    private ArrayList<SoundCloudTrack> mTracks;

    /**
     * Default constructor.
     */
    public SoundCloudPlaylist() {
        mTracks = new ArrayList<>();
    }

    private SoundCloudPlaylist(Parcel in) {
        in.readTypedList(this.mTracks, SoundCloudTrack.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mTracks);
    }

    @Override
    public String toString() {
        return "SoundCloudPlaylist{"
                + "mTracks=" + mTracks
                + '}';
    }

    /**
     * Get the tracks added in the playlist.
     *
     * @return list of tracks.
     */
    public ArrayList<SoundCloudTrack> getTracks() {
        return mTracks;
    }

    /**
     * Add a new track to the playlist.
     *
     * @param track track to add.
     */
    public void addTracks(SoundCloudTrack track) {
        mTracks.add(track);
    }

    /**
     * Add a set a track.
     *
     * @param tracks tracks to be added.
     */
    public void addAllTracks(Collection<SoundCloudTrack> tracks) {
        mTracks.addAll(tracks);
    }
}
