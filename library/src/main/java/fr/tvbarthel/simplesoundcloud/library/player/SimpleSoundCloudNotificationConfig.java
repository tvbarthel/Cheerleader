package fr.tvbarthel.simplesoundcloud.library.player;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBarActivity;

/**
 * Encapsulate notification config.
 */
public class SimpleSoundCloudNotificationConfig implements Parcelable {

    /**
     * Parcelable.
     */
    public static final Parcelable.Creator<SimpleSoundCloudNotificationConfig> CREATOR
            = new Parcelable.Creator<SimpleSoundCloudNotificationConfig>() {
        public SimpleSoundCloudNotificationConfig createFromParcel(Parcel source) {
            return new SimpleSoundCloudNotificationConfig(source);
        }

        public SimpleSoundCloudNotificationConfig[] newArray(int size) {
            return new SimpleSoundCloudNotificationConfig[size];
        }
    };

    /**
     * Icon used in the status bar as well as small right icon on Lollipop.
     */
    private int mNotificationIcon;

    /**
     * Background for the small right icon on Lollipop.
     */
    private int mNotificationIconBackground;

    /**
     * Activity which should be launched when user touch the notification.
     */
    private Class<?> mNotificationActivity;

    /**
     * Default constructor.
     */
    public SimpleSoundCloudNotificationConfig() {
    }

    /**
     * Parcelable.
     *
     * @param in source.
     */
    private SimpleSoundCloudNotificationConfig(Parcel in) {
        mNotificationIcon = in.readInt();
        mNotificationIconBackground = in.readInt();
        mNotificationActivity = (Class<?>) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mNotificationIcon);
        dest.writeInt(mNotificationIconBackground);
        dest.writeSerializable(mNotificationActivity);
    }

    /**
     * Icon used in the status bar as well as small right icon on Lollipop.
     *
     * @return icon res id.
     */
    public int getNotificationIcon() {
        return mNotificationIcon;
    }

    /**
     * Icon used in the status bar as well as small right icon on Lollipop.
     *
     * @param notificationIcon icon res id.
     */
    public void setNotificationIcon(@DrawableRes int notificationIcon) {
        mNotificationIcon = notificationIcon;
    }

    /**
     * Background for the small right icon on Lollipop.
     *
     * @return icon background res id.
     */
    public int getNotificationIconBackground() {
        return mNotificationIconBackground;
    }

    /**
     * Background for the small right icon on Lollipop.
     *
     * @param notificationIconBackground icon background res id.
     */
    public void setNotificationIconBackground(@DrawableRes int notificationIconBackground) {
        mNotificationIconBackground = notificationIconBackground;
    }

    /**
     * Activity which should be launched when user touch the notification.
     *
     * @return activity which should be started when user touch the notification.
     */
    public Class<?> getNotificationActivity() {
        return mNotificationActivity;
    }

    /**
     * Activity which should be launched when user touch the notification.
     *
     * @param notificationActivity activity which should be started when user touch the notification.
     */
    public void setNotificationActivity(Activity notificationActivity) {
        mNotificationActivity = notificationActivity.getClass();
    }

    /**
     * Activity which should be launched when user touch the notification.
     *
     * @param notificationActivity activity which should be started when user touch the notification.
     */
    public void setNotificationActivity(ActionBarActivity notificationActivity) {
        mNotificationActivity = notificationActivity.getClass();
    }
}
