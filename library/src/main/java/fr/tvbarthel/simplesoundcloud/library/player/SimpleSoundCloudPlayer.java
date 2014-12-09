package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

/**
 * Service used as SoundCloudPlayer.
 */
public class SimpleSoundCloudPlayer extends Service {

    /**
     * Log cat and thread name prefix.
     */
    private static final String TAG = SimpleSoundCloudPlayer.class.getSimpleName();

    /**
     * Name for the internal handler thread.
     */
    private static final String THREAD_NAME = TAG + "player_thread";

    /**
     * Action used to add a sound cloud track to the queue.
     */
    private static final String ACTION_ADD_TRACK = "sound_cloud_player_action_add_track";

    /**
     * Action used to remove a sound cloud track from the queue.
     */
    private static final String ACTION_REMOVE_TRACK = "sound_cloud_player_action_remove_track";

    /**
     * Action used to start the sound cloud player.
     */
    private static final String ACTION_START_PLAYER = "sound_cloud_player_start";

    /**
     * Action used to pause the sound cloud player.
     */
    private static final String ACTION_PAUSE_PLAYER = "sound_cloud_player_pause";

    /**
     * Action used to skip to the next track of the sound cloud player.
     */
    private static final String ACTION_NEXT_TRACK = "sound_cloud_player_next";

    /**
     * Action used to skip to the previous track of the sound cloud player.
     */
    private static final String ACTION_PREVIOUS_TRACK = "sound_cloud_player_previous";

    /**
     * Action used to change the cursor of the current track.
     */
    private static final String ACTION_SEEK_TO = "sound_cloud_player_seek_to";

    /**
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Bundle key used to pass track id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW = "sound_cloud_player_bundle_key_play_now";

    /**
     * Bundle key used to pass "play now" policy.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_ID = "sound_cloud_player_bundle_key_track_id";

    /**
     * Bundle key used to seek to a given position.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION = "sound_cloud_player_bundle_key_seek_to";


    /**
     * what id used to identify "start player" message.
     */
    private static final int WHAT_START_PLAYER = 0;

    /**
     * what id used to identify "pause player" message.
     */
    private static final int WHAT_PAUSE_PLAYER = 1;

    /**
     * what id used to identify "next track" message.
     */
    private static final int WHAT_NEXT_TRACK = 2;

    /**
     * what id used to identify "previous track" message.
     */
    private static final int WHAT_PREVIOUS_TRACK = 3;

    /**
     * what id used to identify "seek to" message.
     */
    private static final int WHAT_SEEK_TO = 4;

    /**
     * what id used to identify add track message.
     */
    private static final int WHAT_ADD_TRACK = 5;

    /**
     * what id used to identify remove track message.
     */
    private static final int WHAT_REMOVE_TRACK = 6;

    /**
     * Handler used to execute works on an {@link android.os.HandlerThread}
     */
    private Handler mPlayerHandler;

    /**
     * Start the playback. First track of the queue will be played.
     * <p/>
     * If the SoundCloud player is currently paused, the current track will be restart at the stopped position.
     *
     * @param context context from which the service will be started.
     */
    public static void play(Context context) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_START_PLAYER);
        context.startService(intent);
    }

    /**
     * Pause the SoundCloud payer.
     *
     * @param context context from which the service will be started.
     */
    public static void pause(Context context) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PAUSE_PLAYER);
        context.startService(intent);
    }

    /**
     * Stop the current played track and load the next one if the playlist isn't empty.
     * <p/>
     * If the current played track is the last one, the first track will be loaded.
     *
     * @param context context from which the service will be started.
     */
    public static void next(Context context) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_NEXT_TRACK);
        context.startService(intent);
    }

    /**
     * Stop the current played track and load the previous one.
     * <p/>
     * If the current played track is the first one, the last track will be loaded.
     *
     * @param context context from which the service will be started.
     */
    public static void previous(Context context) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_PREVIOUS_TRACK);
        context.startService(intent);
    }

    /**
     * Seek to the precise track position.
     * <p/>
     * The current playing state of the SoundCloud player will be kept.
     * <p/>
     * If playing it remains playing, if paused it remains paused.
     *
     * @param context context from which the service will be started.
     * @param milli   time in milli of the position.
     */
    public static void seekTo(Context context, int milli) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_SEEK_TO);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION, milli);
        context.startService(intent);
    }

    /**
     * Add a track to the player queue.
     *
     * @param context  context from which the service will be started.
     * @param clientId soundCloud client id to communicate with the api.
     * @param trackId  sound cloud track id to be played.
     * @param playNow  true to play the track immediately.
     */
    public static void addTrack(Context context, String clientId, int trackId, boolean playNow) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId can't be null.");
        }
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_ADD_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_ID, trackId);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW, playNow);
        context.startService(intent);
    }

    /**
     * Remove track from the player playlist.
     *
     * @param context context from which the service will be started.
     * @param trackId track id to remove.
     */
    public static void removeTrack(Context context, int trackId) {
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_REMOVE_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_TRACK_ID, trackId);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_AUDIO);
        thread.start();

        mPlayerHandler = new PlayerHandler(thread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message message = mPlayerHandler.obtainMessage();
            message.setData(intent.getExtras());
            switch (intent.getAction()) {
                case ACTION_START_PLAYER:
                    message.what = WHAT_START_PLAYER;
                    break;
                case ACTION_PAUSE_PLAYER:
                    message.what = WHAT_PAUSE_PLAYER;
                    break;
                case ACTION_NEXT_TRACK:
                    message.what = WHAT_NEXT_TRACK;
                    break;
                case ACTION_PREVIOUS_TRACK:
                    message.what = WHAT_PREVIOUS_TRACK;
                    break;
                case ACTION_SEEK_TO:
                    message.what = WHAT_SEEK_TO;
                    break;
                case ACTION_ADD_TRACK:
                    message.what = WHAT_ADD_TRACK;
                    break;
                case ACTION_REMOVE_TRACK:
                    message.what = WHAT_REMOVE_TRACK;
                    break;
                default:
                    break;
            }
            mPlayerHandler.sendMessage(message);
        }
        return START_STICKY;
    }

    private void startPlayer() {
        // TODO implement
        Log.d("DEBUG===", "startPlayer");
    }

    private void stopPlayer() {
        // TODO implement
        Log.d("DEBUG===", "stopPlayer");
    }

    private void nextTrack() {
        // TODO implement
        Log.d("DEBUG===", "nextTrack");
    }

    private void previousTrack() {
        // TODO implement
        Log.d("DEBUG===", "previousTrack");
    }

    private void seekToPosition(int milli) {
        // TODO implement
        Log.d("DEBUG===", "seekToPosition : " + milli);
    }

    private void enqueueTrack(String clientId, int trackId) {
        // TODO implement
        Log.d("DEBUG==", "add track : " + clientId + " : " + trackId);
    }

    private void removeTrack(int trackId) {
        // TODO implement
        Log.d("DEBUG===", "removeTrack : " + trackId);
    }


    /**
     * Looper used process player request.
     */
    private final class PlayerHandler extends Handler {

        /**
         * Handler used to process player request.
         *
         * @param looper must not be null.
         */
        public PlayerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case WHAT_START_PLAYER:
                    startPlayer();
                    break;
                case WHAT_PAUSE_PLAYER:
                    stopPlayer();
                    break;
                case WHAT_NEXT_TRACK:
                    nextTrack();
                    break;
                case WHAT_PREVIOUS_TRACK:
                    previousTrack();
                    break;
                case WHAT_SEEK_TO:
                    seekToPosition(data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_POSITION));
                    break;
                case WHAT_ADD_TRACK:
                    enqueueTrack(
                            data.getString(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID),
                            data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_ID)
                    );
                    if (data.getBoolean(BUNDLE_KEY_SOUND_CLOUD_PLAY_NOW)) {
                        startPlayer();
                    }
                    break;
                case WHAT_REMOVE_TRACK:
                    removeTrack(data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_ID));
                    break;
                default:
                    break;
            }
        }
    }
}
