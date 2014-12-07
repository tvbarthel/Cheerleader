package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
     * Action used to identify start request.
     */
    private static final String ACTION_START = "sound_cloud_player_action_start";

    /**
     * Bundle key used to pass client id.
     */
    private static final String BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID = "sound_cloud_player_bundle_key_client_id";

    /**
     * Handler used to execute works on an {@link android.os.HandlerThread}
     */
    private Handler mPlayerHandler;

    /**
     * SoundCloud client id deliver to access the SoundCloud API.
     */
    private String mSoundCloudClientId;

    /**
     * Start the player service.
     *
     * @param context context from which the service will be started.
     */
    public static void start(Context context, String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId can't be null.");
        }
        Intent intent = new Intent(context, SimpleSoundCloudPlayer.class);
        intent.setAction(ACTION_START);
        intent.putExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID, clientId);
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
            switch (intent.getAction()) {
                case ACTION_START:
                    mSoundCloudClientId = intent.getStringExtra(BUNDLE_KEY_SOUND_CLOUD_CLIENT_ID);
                    Log.d(TAG, "---> Start");
                    Log.d(TAG, "Client id : " + mSoundCloudClientId);
                    Log.d(TAG, "<--- Start");
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    /**
     * Looper used process player request.
     */
    private static class PlayerHandler extends Handler {

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
        }
    }
}
