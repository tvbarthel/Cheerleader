package fr.tvbarthel.simplesoundcloud.library.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudPlaylist;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Listener used to catch event send by the
 * {@link fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudListener}
 */
public class SimpleSoundCloudListener extends BroadcastReceiver {

    /**
     * package private, action used when playlist is retrieved.
     */
    static final String ACTION_ON_PLAYLIST_RETRIEVED = "simple_sc_listener_action_on_playlist_retrieved";

    /**
     * package private, action used when the player started a track.
     */
    static final String ACTION_ON_TRACK_PLAYED = "simple_sc_listener_action_on_track_played";

    /**
     * package private, action used when the player paused.
     */
    static final String ACTION_ON_PLAYER_PAUSED = "simple_sc_listener_action_on_track_paused";

    /**
     * package private, action used when a track has been added to the player.
     */
    static final String ACTION_ON_TRACK_ADDED = "simple_sc_listener_action_on_track_added";

    /**
     * package private, action used when a track has been added to the player.
     */
    static final String ACTION_ON_TRACK_REMOVED = "simple_sc_listener_action_on_track_removed";

    /**
     * package private, action used when the player completed a seek action.
     */
    static final String ACTION_ON_SEEK_COMPLETE = "simple_sc_listener_action_on_player_seek_complete";

    /**
     * package private, extra key for passing a track.
     */
    static final String EXTRA_KEY_TRACK = "simple_sc_listener_extra_track";

    /**
     * package private, extra key for passing the playlist.
     */
    static final String EXTRA_KEY_PLAYLIST = "simple_sc_listener_extra_playlist";

    /**
     * package private, extra key for passing index.
     */
    static final String EXTRA_KEY_INDEX = "simple_sc_listener_extra_index";

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
                case ACTION_ON_PLAYLIST_RETRIEVED:
                    onPlaylistRetrieved(
                            ((SoundCloudPlaylist) intent.getParcelableExtra(EXTRA_KEY_PLAYLIST)),
                            intent.getIntExtra(EXTRA_KEY_INDEX, 0));
                    break;
                case ACTION_ON_TRACK_PLAYED:
                    onPlay(
                            ((SoundCloudTrack) intent.getParcelableExtra(EXTRA_KEY_TRACK)),
                            intent.getIntExtra(EXTRA_KEY_INDEX, 0)
                    );
                    break;
                case ACTION_ON_PLAYER_PAUSED:
                    onPause();
                    break;
                case ACTION_ON_TRACK_ADDED:
                    onTrackAdded(
                            ((SoundCloudTrack) intent.getParcelableExtra(EXTRA_KEY_TRACK)),
                            intent.getIntExtra(EXTRA_KEY_INDEX, 0)
                    );
                    break;
                case ACTION_ON_TRACK_REMOVED:
                    onTrackRemoved(
                            ((SoundCloudTrack) intent.getParcelableExtra(EXTRA_KEY_TRACK)),
                            intent.getIntExtra(EXTRA_KEY_INDEX, 0)
                    );
                    break;
                case ACTION_ON_SEEK_COMPLETE:
                    onSeekTo(intent.getIntExtra(EXTRA_KEY_SEEK, 0));
                    break;
                default:
                    Log.e(TAG, "unknown action : " + intent.getAction());
                    break;
            }
        }
    }

    /**
     * Async callback for internal playlist request.
     *
     * @param playlist     {@link fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack}
     *                     currently added to the player.
     * @param currentTrack position of the current track in the playlist.
     */
    protected void onPlaylistRetrieved(SoundCloudPlaylist playlist, int currentTrack) {

    }

    /**
     * Called when a track starts to be played.
     *
     * @param track    played track.
     * @param position position of the track in the playlist.
     */
    protected void onPlay(SoundCloudTrack track, int position) {

    }

    /**
     * Called when a the player has been paused.
     */
    protected void onPause() {

    }

    /**
     * Called when a track has been added to the player playlist.
     *
     * @param track    track added to the player.
     * @param position position of the track in the playlist.
     */
    protected void onTrackAdded(SoundCloudTrack track, int position) {

    }

    /**
     * Called when a track has been removed from the player playlist.
     *
     * @param track    track removed.
     * @param position position of the track in the playlist.
     */
    protected void onTrackRemoved(SoundCloudTrack track, int position) {

    }

    /**
     * Called when the player complete a seek action.
     *
     * @param milli time in milli of the seek.
     */
    protected void onSeekTo(int milli) {

    }
}
