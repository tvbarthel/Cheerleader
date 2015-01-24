package fr.tvbarthel.simplesoundcloud.library.player;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import fr.tvbarthel.simplesoundcloud.library.R;
import fr.tvbarthel.simplesoundcloud.library.helpers.SoundCloudArtworkHelper;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Handle player notification behaviour.
 */
class SimpleSoundCloudNotificationManager {

    /**
     * Notification ID.
     */
    private static final int NOTIFICATION_ID = 0x00000042;

    /**
     * Playback pending intent request code.
     */
    private static final int REQUEST_CODE_PLAYBACK = 0x00000010;

    /**
     * Handler running on main thread to perform change on notification ui.
     */
    private Handler mMainThreadHandler;

    /**
     * Target used to load asynchronously track artwork into the notification.
     */
    private Target mArtworkTarget;

    /**
     * Id of the track displayed in the notification.
     */
    private int mTrackId;

    /**
     * Style used to display the track image into the notification.
     */
    private NotificationCompat.BigPictureStyle mBigPictureStyle;

    /**
     * Builder used to build notification.
     */
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * System service to manage notification.
     */
    private NotificationManager mNotificationManager;

    /**
     * Pending intent set to the playback button.
     */
    private PendingIntent mTogglePlaybackPendingIntent;

    /**
     * Encapsulate player notification behaviour.
     *
     * @param context context used to instantiate internal component.
     */
    public SimpleSoundCloudNotificationManager(Context context) {

        mTrackId = -1;

        mMainThreadHandler = new Handler(context.getApplicationContext().getMainLooper());
        mBigPictureStyle = new NotificationCompat.BigPictureStyle();

        mNotificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        // initialize actions' PendingIntents.
        initializePendingIntent(context);

        // initialize traget used to load artwork asynchronously.
        initializeArtworkTarget();
    }

    /**
     * Post a notification displaying the given track in the status bare.
     *
     * @param service       service started as foreground if no dismissible.
     * @param track         track displayed.
     * @param isPaused      true if the current player is paused. Then play action will be displayed.
     *                      Otherwise, pause action will be displayed.
     * @param isDismissible true if the notification can be dismissed.
     */
    public void notify(final Service service, final SoundCloudTrack track, boolean isPaused, boolean isDismissible) {

        // used to reset actions since removing action is not allowed.
        resetBuilder(service);

        // set the title
        mNotificationBuilder.setContentTitle(track.getTitle());

        // set the right icon for the toggle playback action.
        if (isPaused) {
            mNotificationBuilder.addAction(R.drawable.simple_sound_cloud_notification_play, "",
                    mTogglePlaybackPendingIntent);
        } else {
            mNotificationBuilder.addAction(R.drawable.simple_sound_cloud_notification_pause, "",
                    mTogglePlaybackPendingIntent);
        }

        // set the dismiss policy.
        if (isDismissible) {
            mNotificationBuilder.setOngoing(false);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        } else {
            service.startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        }

        // since toggle playback is often pressed for the same track, only load the artwork when a
        // new track is passed.
        int newTrackId = track.getId();
        if (mTrackId == -1 || mTrackId != newTrackId) {
            loadArtwork(
                    service,
                    SoundCloudArtworkHelper.getArtworkUrl(track, SoundCloudArtworkHelper.XLARGE)
            );
            mTrackId = newTrackId;
        }
    }

    /**
     * Initialize {@link android.app.PendingIntent} used for notification actions.
     *
     * @param context context used to instantiate intent.
     */
    private void initializePendingIntent(Context context) {

        // toggle playback
        Intent togglePlaybackIntent = new Intent(context, SimpleSoundCloudPlayer.class);
        togglePlaybackIntent.setAction(SimpleSoundCloudPlayer.ACTION_TOGGLE_PLAYBACK);
        mTogglePlaybackPendingIntent = PendingIntent.getService(context, REQUEST_CODE_PLAYBACK,
                togglePlaybackIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Initialize target used to load artwork asynchronously.
     */
    private void initializeArtworkTarget() {
        mArtworkTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBigPictureStyle.bigPicture(bitmap);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }

    /**
     * Reset the notification builder to remove all actions.
     *
     * @param context context used to instantiate the builder.
     */
    private void resetBuilder(Context context) {
        mNotificationBuilder = new NotificationCompat.Builder(context)
                .setStyle(mBigPictureStyle)
                .setSmallIcon(R.drawable.simple_sound_cloud_notification_icon);
    }

    /**
     * Load the track artwork.
     *
     * @param context    context used by {@link com.squareup.picasso.Picasso} to load the artwork asynchronously.
     * @param artworkUrl artwork url of the track.
     */
    private void loadArtwork(final Context context, final String artworkUrl) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Picasso
                        .with(context)
                        .load(artworkUrl)
                        .into(mArtworkTarget);
            }
        });
    }
}
