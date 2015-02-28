package fr.tvbarthel.simplesoundcloud.library.player;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import fr.tvbarthel.simplesoundcloud.library.R;
import fr.tvbarthel.simplesoundcloud.library.helpers.SoundCloudArtworkHelper;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;

/**
 * Handle player notification behaviour.
 */
public class SimpleSoundCloudNotificationManager {

    /**
     * Notification ID.
     */
    private static final int NOTIFICATION_ID = 0x00000042;

    /**
     * Playback pending intent request code.
     */
    private static final int REQUEST_CODE_PLAYBACK = 0x00000010;

    /**
     * Next track pending intent request code.
     */
    private static final int REQUEST_CODE_NEXT = 0x00000020;

    /**
     * Previous track pending intent request code.
     */
    private static final int REQUEST_CODE_PREVIOUS = 0x00000030;

    /**
     * Clear pending intent request code.
     */
    private static final int REQUEST_CODE_CLEAR = 0x00000040;

    /**
     * Drawable used as icon for notification.
     */
    private static int sNotificationIcon;

    /**
     * Color used on Lollipop device as background for notification icon.
     */
    private static int sNotificationIconBackground = -1;

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
     * Builder used to build notification.
     */
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * {@link android.widget.RemoteViews} set to the notification.
     */
    private RemoteViews mNotificationView;

    /**
     * {@link android.widget.RemoteViews} set as expanded notification content.
     */
    private RemoteViews mNotificationExpandedView;

    /**
     * System service to manage notification.
     */
    private NotificationManager mNotificationManager;

    /**
     * Pending intent set to the playback button.
     */
    private PendingIntent mTogglePlaybackPendingIntent;

    /**
     * Pending intent set to the next button.
     */
    private PendingIntent mNextPendingIntent;

    /**
     * Pending intent set to the previous button.
     */
    private PendingIntent mPreviousPendingIntent;

    /**
     * Pending intent set to clear the player.
     */
    private PendingIntent mClearPendingIntent;

    /**
     * Pending intent used to launch the player activity when the user press the notification.
     */
    private PendingIntent mContentIntent;

    /**
     * Encapsulate player notification behaviour.
     *
     * @param context context used to instantiate internal component.
     */
    public SimpleSoundCloudNotificationManager(Context context) {

        mTrackId = -1;

        mMainThreadHandler = new Handler(context.getApplicationContext().getMainLooper());

        mNotificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        // initialize actions' PendingIntents.
        initializePendingIntent(context);

        // initialize traget used to load artwork asynchronously.
        initializeArtworkTarget();

        initNotificationBuilder(context);
    }

    /**
     * Set the icon display in the notification.
     *
     * @param iconResId res id of the notification icon.
     */
    public static void setNotificationIcon(int iconResId) {
        sNotificationIcon = iconResId;
    }

    /**
     * Only for Lollipop devices.
     * <p/>
     * Set the background drawable of the small icon.
     *
     * @param resId small right icon background.
     */
    public static void setNotificationIconBackground(@DrawableRes int resId) {
        sNotificationIconBackground = resId;
    }

    /**
     * Set the pending intent used when user pressed the notification.
     *
     * @param contentIntent pending intent used to launch player activity
     *                      when user press the notification.
     */
    public void setContentIntent(PendingIntent contentIntent) {
        mContentIntent = contentIntent;
    }

    /**
     * Post a notification displaying the given track in the status bare.
     *
     * @param service  service started as foreground if no dismissible.
     * @param track    track displayed.
     * @param isPaused true if the current player is paused. Then play action will be displayed.
     *                 Otherwise, pause action will be displayed.
     */
    public void notify(final Service service, final SoundCloudTrack track, boolean isPaused) {


        // set the title
        mNotificationView.setTextViewText(R.id.simple_sound_cloud_notification_title, track.getArtist());
        mNotificationView.setTextViewText(R.id.simple_sound_cloud_notification_subtitle, track.getTitle());
        mNotificationExpandedView.setTextViewText(R.id.simple_sound_cloud_notification_title, track.getArtist());
        mNotificationExpandedView.setTextViewText(R.id.simple_sound_cloud_notification_subtitle, track.getTitle());

        // set the right icon for the toggle playback action.
        if (isPaused) {
            mNotificationView.setImageViewResource(
                    R.id.simple_sound_cloud_notification_play,
                    R.drawable.simple_sound_cloud_notification_play
            );
            mNotificationExpandedView.setImageViewResource(
                    R.id.simple_sound_cloud_notification_play,
                    R.drawable.simple_sound_cloud_notification_play
            );
        } else {
            mNotificationView.setImageViewResource(
                    R.id.simple_sound_cloud_notification_play,
                    R.drawable.simple_sound_cloud_notification_pause
            );
            mNotificationExpandedView.setImageViewResource(
                    R.id.simple_sound_cloud_notification_play,
                    R.drawable.simple_sound_cloud_notification_pause
            );
        }

        service.startForeground(NOTIFICATION_ID, buildNotification());

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
     * Cancel the player notification.
     */
    public void cancel() {
        mNotificationManager.cancel(NOTIFICATION_ID);
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

        // next track
        Intent nextPrendingIntent = new Intent(context, SimpleSoundCloudPlayer.class);
        nextPrendingIntent.setAction(SimpleSoundCloudPlayer.ACTION_NEXT_TRACK);
        mNextPendingIntent = PendingIntent.getService(context, REQUEST_CODE_NEXT,
                nextPrendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // previous track
        Intent previousPendingIntent = new Intent(context, SimpleSoundCloudPlayer.class);
        previousPendingIntent.setAction(SimpleSoundCloudPlayer.ACTION_PREVIOUS_TRACK);
        mPreviousPendingIntent = PendingIntent.getService(context, REQUEST_CODE_PREVIOUS,
                previousPendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // clear notification
        Intent clearPendingIntent = new Intent(context, SimpleSoundCloudPlayer.class);
        clearPendingIntent.setAction(SimpleSoundCloudPlayer.ACTION_CLEAR_NOTIFICATION);
        mClearPendingIntent = PendingIntent.getService(context, REQUEST_CODE_CLEAR,
                clearPendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Initialize target used to load artwork asynchronously.
     */
    private void initializeArtworkTarget() {
        mArtworkTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mNotificationView.setImageViewBitmap(R.id.simple_sound_cloud_notification_thumbnail, bitmap);
                mNotificationExpandedView.setImageViewBitmap(
                        R.id.simple_sound_cloud_notification_thumbnail, bitmap);
                mNotificationExpandedView.setImageViewBitmap(
                        R.id.simple_sound_cloud_notification_expanded_thumbnail, bitmap);
                mNotificationManager.notify(NOTIFICATION_ID, buildNotification());
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
     * Init all static components of the notification.
     *
     * @param context context used to instantiate the builder.
     */
    private void initNotificationBuilder(Context context) {

        // inti builder.
        mNotificationBuilder = new NotificationCompat.Builder(context);
        mNotificationView = new RemoteViews(context.getPackageName(),
                R.layout.simple_sound_cloud_notification);
        mNotificationExpandedView = new RemoteViews(context.getPackageName(),
                R.layout.simple_sound_cloud_notification_expanded);

        // add right icon on Lollipop.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addSmallIcon(mNotificationView);
            addSmallIcon(mNotificationExpandedView);
        }

        // set pending intents
        mNotificationView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_previous, mPreviousPendingIntent);
        mNotificationExpandedView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_previous, mPreviousPendingIntent);
        mNotificationView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_next, mNextPendingIntent);
        mNotificationExpandedView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_next, mNextPendingIntent);
        mNotificationView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_play, mTogglePlaybackPendingIntent);
        mNotificationExpandedView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_play, mTogglePlaybackPendingIntent);
        mNotificationView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_clear, mClearPendingIntent);
        mNotificationExpandedView.setOnClickPendingIntent(
                R.id.simple_sound_cloud_notification_clear, mClearPendingIntent);

        // add icon for action bar.
        mNotificationBuilder.setSmallIcon(sNotificationIcon);

        // set the remote view.
        mNotificationBuilder.setContent(mNotificationView);

        // add pending intent used when notification is pressed.
        if (mContentIntent != null) {
            mNotificationBuilder.setContentIntent(mContentIntent);
        }
    }

    /**
     * Build the notification with the internal {@link android.app.Notification.Builder}
     *
     * @return notification ready to be displayed.
     */
    private Notification buildNotification() {
        Notification notification = mNotificationBuilder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = mNotificationExpandedView;
        }
        return notification;
    }

    /**
     * Add the small right icon for Lollipop device.
     *
     * @param notificationView remotesview used in the notification.
     */
    private void addSmallIcon(RemoteViews notificationView) {
        notificationView.setInt(R.id.simple_sound_cloud_notification_icon,
                "setBackgroundResource", sNotificationIconBackground);
        notificationView.setImageViewResource(R.id.simple_sound_cloud_notification_icon,
                sNotificationIcon);
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
