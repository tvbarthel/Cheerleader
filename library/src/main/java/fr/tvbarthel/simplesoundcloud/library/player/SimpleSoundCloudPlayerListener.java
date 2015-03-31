package fr.tvbarthel.simplesoundcloud.library.player;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;

/**
 * Listener interface used to catch {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer}
 * events.
 */
public interface SimpleSoundCloudPlayerListener {
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

    /**
     * Called when the player paused due to buffering more data.
     */
    public void onBufferingStarted();

    /**
     * Called when the player resumed due after having buffered enough data.
     */
    public void onBufferingEnded();

    /**
     * Called when current position time changed.
     *
     * @param milli current time in milli seconds.
     */
    public void onProgressChanged(int milli);

}
