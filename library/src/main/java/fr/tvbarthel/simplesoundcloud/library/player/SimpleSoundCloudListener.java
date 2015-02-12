package fr.tvbarthel.simplesoundcloud.library.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Listener used to catch event send by the
 * {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudListener}
 */
public class SimpleSoundCloudListener extends BroadcastReceiver {

    /**
     * package private, action used when the player started a track.
     */
    static final String ACTION_ON_TRACK_PLAYED = "simple_sc_listener_action_on_track_played";

    /**
     * package private, action used when the player paused.
     */
    static final String ACTION_ON_PLAYER_PAUSED = "simple_sc_listener_action_on_track_paused";

    /**
     * package private, action used when the player completed a seek action.
     */
    static final String ACTION_ON_SEEK_COMPLETE = "simple_sc_listener_action_on_player_seek_complete";

    /**
     * package private, action used when the player has been destroyed
     */
    static final String ACTION_ON_PLAYER_DESTROYED = "simple_sc_listener_action_on_player_destroyed";

    /**
     * package private, extra key for passing a track.
     */
    static final String EXTRA_KEY_TRACK = "simple_sc_listener_extra_track";

    /**
     * package private, extra key for passing seek time
     */
    static final String EXTRA_KEY_SEEK = "simple_sc_listener_extra_seek";

    /**
     * Log cat.
     */
    private static final String TAG = SimpleSoundCloudListener.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_ON_TRACK_PLAYED:
                    onPlay(((SoundCloudTrack) intent.getParcelableExtra(EXTRA_KEY_TRACK)));
                    break;
                case ACTION_ON_PLAYER_PAUSED:
                    onPause();
                    break;
                case ACTION_ON_SEEK_COMPLETE:
                    onSeekTo(intent.getIntExtra(EXTRA_KEY_SEEK, 0));
                    break;
                case ACTION_ON_PLAYER_DESTROYED:
                    onPlayerDestroyed();
                    break;
                default:
                    Log.e(TAG, "unknown action : " + intent.getAction());
                    break;
            }
        }
    }

    /**
     * Called when a track starts to be played.
     *
     * @param track played track.
     */
    protected void onPlay(SoundCloudTrack track) {

    }

    /**
     * Called when a the player has been paused.
     */
    protected void onPause() {

    }

    /**
     * Called when the player complete a seek action.
     *
     * @param milli time in milli of the seek.
     */
    protected void onSeekTo(int milli) {

    }

    /**
     * Called when the player has been destroyed.
     */
    protected void onPlayerDestroyed() {

    }
}
