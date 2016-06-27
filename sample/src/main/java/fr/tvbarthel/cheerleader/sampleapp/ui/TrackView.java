package fr.tvbarthel.cheerleader.sampleapp.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;
import fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper;
import fr.tvbarthel.cheerleader.sampleapp.R;

/**
 * Simple View used to render a track.
 */
public class TrackView extends FrameLayout implements View.OnClickListener {

    private ImageView mArtwork;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mDuration;

    private SoundCloudTrack mModel;
    private Listener mListener;

    private int mTrackColor;
    private int mArtistColor;
    private int mDurationColor;
    private int mTrackColorSelected;
    private int mArtistColorSelected;
    private int mDurationColorSelected;

    /**
     * Simple View used to render a track.
     *
     * @param context calling context.
     */
    public TrackView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple View used to render a track.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple View used to render a track.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public TrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mDuration.setTextColor(mDurationColorSelected);
            mArtist.setTextColor(mArtistColorSelected);
            mTitle.setTextColor(mTrackColorSelected);
        } else {
            mDuration.setTextColor(mDurationColor);
            mArtist.setTextColor(mArtistColor);
            mTitle.setTextColor(mTrackColor);
        }
    }

    /**
     * Set the track which must be displayed.
     *
     * @param track view model.
     */
    public void setModel(SoundCloudTrack track) {
        mModel = track;
        if (mModel != null) {
            Picasso.with(getContext())
                    .load(SoundCloudArtworkHelper.getArtworkUrl(mModel, SoundCloudArtworkHelper.XLARGE))
                    .placeholder(R.color.grey_light)
                    .fit()
                    .centerInside()
                    .into(mArtwork);
            mArtist.setText(mModel.getArtist());
            mTitle.setText(mModel.getTitle());
            long min = mModel.getDurationInMilli() / 60000;
            long sec = (mModel.getDurationInMilli() % 60000) / 1000;
            mDuration.setText(String.format(getResources().getString(R.string.duration), min, sec));
        }
    }

    /**
     * Set a listener to catch the view events.
     *
     * @param listener listener to register.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.track_view, this);

        mArtwork = ((ImageView) findViewById(R.id.track_view_artwork));
        mTitle = ((TextView) findViewById(R.id.track_view_title));
        mArtist = ((TextView) findViewById(R.id.track_view_artist));
        mDuration = ((TextView) findViewById(R.id.track_view_duration));

        setBackgroundResource(R.drawable.selectable_background_white);
        int padding = getResources().getDimensionPixelOffset(R.dimen.default_padding);
        setPadding(padding, padding, padding, padding);

        this.setOnClickListener(this);

        Resources res = getResources();
        mTrackColor = res.getColor(R.color.track_view_track);
        mArtistColor = res.getColor(R.color.track_view_artist);
        mDurationColor = res.getColor(R.color.track_view_duration);
        mArtistColorSelected = res.getColor(R.color.track_view_artist_selected);
        mTrackColorSelected = res.getColor(R.color.track_view_track_selected);
        mDurationColorSelected = res.getColor(R.color.track_view_duration_selected);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onTrackClicked(mModel);
        }
    }

    /**
     * Interface used to catch view events.
     */
    public interface Listener {

        /**
         * Called when the user clicked on the track view.
         *
         * @param track model of the view.
         */
        void onTrackClicked(SoundCloudTrack track);
    }
}
