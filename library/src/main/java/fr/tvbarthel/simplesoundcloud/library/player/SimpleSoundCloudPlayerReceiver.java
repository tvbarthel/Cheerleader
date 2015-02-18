package fr.tvbarthel.simplesoundcloud.library.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver used to catch {@link android.media.AudioManager#ACTION_AUDIO_BECOMING_NOISY} events.
 * <p/>
 * Propagate event to the player.
 */
public class SimpleSoundCloudPlayerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // signal player service to stop playback
            Intent i = new Intent(context, SimpleSoundCloudPlayer.class);
            i.setAction(SimpleSoundCloudPlayer.ACTION_AUDIO_BECOMING_NOISY);
            context.startService(i);
        }
    }
}
