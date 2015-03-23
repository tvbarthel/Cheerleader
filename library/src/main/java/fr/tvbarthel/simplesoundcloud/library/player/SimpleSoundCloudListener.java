package fr.tvbarthel.simplesoundcloud.library.player;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Listener interface used to catch {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer}
 * events.
 */
public interface SimpleSoundCloudListener {
    /**
     * Called when a track starts to be played.
     *
     * @param track played track.
     */
    public void onPlayerPlay(SoundCloudTrack track);

    /**
     * Called when a the player has been paused.
     */
    public void onPlayerPause();

    /**
     * Called when the player complete a seek action.
     *
     * @param milli time in milli of the seek.
     */
    public void onPlayerSeekTo(int milli);

    /**
     * Called when the player has been destroyed.
     */
    public void onPlayerDestroyed();
}
