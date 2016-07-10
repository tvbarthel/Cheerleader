package fr.tvbarthel.cheerleader.sampleapp.ui;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fr.tvbarthel.cheerleader.library.client.SoundCloudUser;
import fr.tvbarthel.cheerleader.sampleapp.R;

/**
 * Simple view used to display the details of an artist.
 */
public class ArtistView extends FrameLayout {

    private ImageView mAvatar;
    private TextView mArtistName;
    private TextView mTracks;
    private TextView mDescription;
    private SoundCloudUser mModel;

    /**
     * Simple view used to display the details of an artist.
     *
     * @param context holding context.
     */
    public ArtistView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to display the details of an artist.
     *
     * @param context holding context.
     * @param attrs   attrs from xml.
     */
    public ArtistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to display the details of an artist.
     *
     * @param context      holding context.
     * @param attrs        attrs from xml.
     * @param defStyleAttr style from xml.
     */
    public ArtistView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Set the {@link SoundCloudUser} used as model.
     *
     * @param artist user used as artist.
     */
    public void setModel(SoundCloudUser artist) {
        mModel = artist;
        if (mModel != null) {
            Picasso.with(getContext()).load(mModel.getAvatarUrl()).fit().centerInside().into(mAvatar);
            mArtistName.setText(mModel.getFullName());
            mTracks.setText(
                    String.format(
                            getResources().getString(R.string.artist_view_track_count),
                            mModel.getTrackCount()
                    )
            );
            mDescription.setText(Html.fromHtml(mModel.getDescription()));
            this.setVisibility(VISIBLE);
        }
    }

    /**
     * Initialize internal component.
     *
     * @param context holding context.
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.artist_view, this);
        this.setVisibility(INVISIBLE);
        mAvatar = ((ImageView) findViewById(R.id.artist_view_avatar));
        mArtistName = ((TextView) findViewById(R.id.artist_view_name));
        mTracks = ((TextView) findViewById(R.id.artist_view_track_number));
        mDescription = ((TextView) findViewById(R.id.artist_view_description));
    }
}
