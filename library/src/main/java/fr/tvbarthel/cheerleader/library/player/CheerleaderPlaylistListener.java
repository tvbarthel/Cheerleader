package fr.tvbarthel.cheerleader.library.player;

import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;

/**
 * Listener used to catch events performed on the play playlist.
 */
public interface CheerleaderPlaylistListener {

    /**
     * Called when a tracks has been added to the player playlist.
     *
     * @param track track added.
     */
    public void onTrackAdded(SoundCloudTrack track);


    /**
     * Called when a tracks has been removed from the player playlist.
     *
     * @param track   track removed.
     * @param isEmpty true if the playlist is empty after deletion.
     */
    public void onTrackRemoved(SoundCloudTrack track, boolean isEmpty);
}
