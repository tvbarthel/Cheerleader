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
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Bundle key used to pass track id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_TRACK_ID = "sound_cloud_player_bundle_key_track_id";

    /**
     * what id used to identify add track message.
     */
    private static final int WHAT_ADD_TRACK = 0;

    /**
     * Handler used to execute works on an {@link android.os.HandlerThread}
     */
    private Handler mPlayerHandler;

    /**
     * Add a track to the player queue.
     *
     * @param context  context from which the service will be started.
     * @param clientId soundCloud client id to communicate with the api.
     * @param trackId  sound cloud track id to be played.
     */
    public static void addTrack(Context context, String clientId, int trackId) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId can't be null.");
        }
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_ADD_TRACK);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
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
                case ACTION_ADD_TRACK:
                    message.what = WHAT_ADD_TRACK;
                    break;
                default:
                    break;
            }
            mPlayerHandler.sendMessage(message);
        }
        return START_STICKY;
    }

    private void enqueueTrack(String clientId, int trackId) {
        Log.d("DEBUG==", "add track : " + clientId + " : " + trackId);
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
                case WHAT_ADD_TRACK:
                    enqueueTrack(
                            data.getString(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID),
                            data.getInt(BUNDLE_KEY_SOUND_CLOUD_TRACK_ID)
                    );
                    break;
            }
        }
    }
}
