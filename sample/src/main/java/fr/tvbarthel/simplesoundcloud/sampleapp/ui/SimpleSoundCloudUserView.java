package fr.tvbarthel.simplesoundcloud.sampleapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudUser;
import fr.tvbarthel.simplesoundcloud.sampleapp.R;

/**
 * Simple view used to render data of a user.
 */
public class SimpleSoundCloudUserView extends LinearLayout {

    /**
     * Layout params used to add view.
     */
    private LayoutParams mLayoutParams;

    /**
     * {@link fr.tvbarthel.simplesoundcloud.library.client.SupportSoundCloudArtistClient} which act as model for this view.
     */
    private SoundCloudUser mSoundCloudUser;

    /**
     * Simple view used to render data of a user.
     *
     * @param context calling context.
     */
    public SimpleSoundCloudUserView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * Simple view used to render data of a user.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public SimpleSoundCloudUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * Simple view used to render data of a user.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public SimpleSoundCloudUserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * Set the {@link fr.tvbarthel.simplesoundcloud.sampleapp.ui.SimpleSoundCloudUserView}
     * which will be displayed in the view.
     *
     * @param user
     */
    public void setSoundCloudUser(SoundCloudUser user) {
        mSoundCloudUser = user;
        this.removeAllViews();

        TextView id = new TextView(getContext());
        id.setText(String.format(getContext().getString(R.string.id), mSoundCloudUser.getId()));

        this.addView(id, mLayoutParams);
    }

    /**
     * Initialize the view.
     */
    private void init() {
        this.setOrientation(LinearLayout.VERTICAL);
        mLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
