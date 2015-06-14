package fr.tvbarthel.cheerleader.library.player;

import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;

/**
 * Listener interface used to catch {@link CheerleaderPlayer}
 * events.
 */
public interface CheerleaderPlayerListener {
    /**
     * Called when a track starts to be played.
     *
     * @param track    played track.
     * @param position position of the played track in the playlist.
     */
    void onPlayerPlay(SoundCloudTrack track, int position);

    /**
     * Called when a the player has been paused.
     */
    void onPlayerPause();

    /**
     * Called when the player complete a seek action.
     *
     * @param milli time in milli of the seek.
     */
    void onPlayerSeekTo(int milli);

    /**
     * Called when the player has been destroyed.
     */
    void onPlayerDestroyed();

    /**
     * Called when the player paused due to buffering more data.
     */
    void onBufferingStarted();

    /**
     * Called when the player resumed due after having buffered enough data.
     */
    void onBufferingEnded();

    /**
     * Called when current position time changed.
     *
     * @param milli current time in milli seconds.
     */
    void onProgressChanged(int milli);

}
