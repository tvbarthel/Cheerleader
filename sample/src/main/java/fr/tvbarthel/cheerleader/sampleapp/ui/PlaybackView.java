package fr.tvbarthel.cheerleader.sampleapp.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;
import fr.tvbarthel.cheerleader.library.helpers.SoundCloudArtworkHelper;
import fr.tvbarthel.cheerleader.library.player.CheerleaderPlayer;
import fr.tvbarthel.cheerleader.library.player.CheerleaderPlayerListener;
import fr.tvbarthel.cheerleader.sampleapp.R;

/**
 * Simple view used to display basic player button : play/pause, next and previous.
 */
public class PlaybackView extends FrameLayout implements View.OnClickListener,
        CheerleaderPlayerListener, SeekBar.OnSeekBarChangeListener {

    private ImageView mArtwork;
    private TextView mTitle;
    private TextView mCurrentTime;
    private TextView mDuration;
    private ImageView mPlayPause;
    private SeekBar mSeekBar;
    private boolean mSeeking;

    /**
     * Dummy listener.
     */
    private static Listener sDummyListener = new Listener() {
        @Override
        public void onTogglePlayPressed() {

        }

        @Override
        public void onPreviousPressed() {

        }

        @Override
        public void onNextPressed() {

        }

        @Override
        public void onSeekToRequested(int milli) {

        }
    };

    /**
     * Current listener object.
     */
    private Listener mListener = sDummyListener;

    /**
     * Simple view used to display basic player button : play/pause, next and previous.
     *
     * @param context calling context.
     */
    public PlaybackView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to display basic player button : play/pause, next and previous.
     *
     * @param context calling context.
     * @param attrs   attr from xml.
     */
    public PlaybackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Simple view used to display basic player button : play/pause, next and previous.
     *
     * @param context      calling context.
     * @param attrs        attr from xml.
     * @param defStyleAttr style from xml.
     */
    public PlaybackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Allow to catch event performed on the view.
     *
     * @param listener listener.
     */
    public void setListener(Listener listener) {
        if (listener == null) {
            mListener = sDummyListener;
        } else {
            mListener = listener;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playback_view_next:
                mListener.onNextPressed();
                break;
            case R.id.playback_view_previous:
                mListener.onPreviousPressed();
                break;
            case R.id.playback_view_toggle_play:
                mListener.onTogglePlayPressed();
                break;

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Player callback ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPlayerPlay(SoundCloudTrack track, int position) {
        setTrack(track);
    }

    @Override
    public void onPlayerPause() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onPlayerSeekTo(int milli) {
        mSeekBar.setProgress(milli);
    }

    @Override
    public void onPlayerDestroyed() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onBufferingStarted() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onBufferingEnded() {
        mPlayPause.setImageResource(R.drawable.ic_pause_white);
    }

    @Override
    public void onProgressChanged(int milli) {
        if(!mSeeking){
            mSeekBar.setProgress(milli);
            int[] secondMinute = getSecondMinutes(milli);
            String duration = String.format(getResources().getString(R.string.playback_view_time), secondMinute[0], secondMinute[1]);
            mCurrentTime.setText(duration);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// SeekBar listener /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int[] secondMinute = getSecondMinutes(progress);
        mCurrentTime.setText(
                String.format(getResources().getString(R.string.playback_view_time),
                        secondMinute[0], secondMinute[1])
        );
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeeking = false;
        mListener.onSeekToRequested(seekBar.getProgress());
    }

    /**
     * Synchronize the player view with the current player state.
     * <p/>
     * Basically, check if a track is loaded as well as the playing state.
     *
     * @param player player currently used.
     */
    public void synchronize(CheerleaderPlayer player) {
        setTrack(player.getCurrentTrack());
        setPlaying(player.isPlaying());
    }

    /**
     * Set the current played track.
     *
     * @param track track which is played.
     */
    private void setTrack(SoundCloudTrack track) {
        if (track == null) {
            mTitle.setText("");
            mArtwork.setImageDrawable(null);
            mPlayPause.setImageResource(R.drawable.ic_play_white);
            mSeekBar.setProgress(0);
            String none = String.format(getResources().getString(R.string.playback_view_time), 0, 0);
            mCurrentTime.setText(none);
            mDuration.setText(none);
        } else {
            Picasso.with(getContext())
                    .load(SoundCloudArtworkHelper.getArtworkUrl(track, SoundCloudArtworkHelper.XLARGE))
                    .fit()
                    .centerCrop()
                    .placeholder(R.color.grey)
                    .into(mArtwork);
            mTitle.setText(Html.fromHtml(String.format(getResources().getString(R.string.playback_view_title),
                    track.getArtist(), track.getTitle())));
            mPlayPause.setImageResource(R.drawable.ic_pause_white);
            if (getTranslationY() != 0) {
                this.animate().translationY(0);
            }
            mSeekBar.setMax(((int) track.getDurationInMilli()));
            String none = String.format(getResources().getString(R.string.playback_view_time), 0, 0);
            int[] secondMinute = getSecondMinutes(track.getDurationInMilli());
            String duration = String.format(getResources().getString(R.string.playback_view_time),
                    secondMinute[0], secondMinute[1]);
            mCurrentTime.setText(none);
            mDuration.setText(duration);
        }
    }

    /**
     * Used to update the play/pause button.
     * <p/>
     * Should be synchronize with the player playing state.
     * See also : {@link CheerleaderPlayer#isPlaying()}.
     *
     * @param isPlaying true if a track is currently played.
     */
    private void setPlaying(boolean isPlaying) {
        if (isPlaying) {
            mPlayPause.setImageResource(R.drawable.ic_pause_white);
        } else {
            mPlayPause.setImageResource(R.drawable.ic_play_white);
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.playback_view, this);
        findViewById(R.id.playback_view_next).setOnClickListener(this);
        findViewById(R.id.playback_view_previous).setOnClickListener(this);
        mCurrentTime = ((TextView) findViewById(R.id.playback_view_current_time));
        mDuration = ((TextView) findViewById(R.id.playback_view_duration));
        mPlayPause = ((ImageView) findViewById(R.id.playback_view_toggle_play));
        mPlayPause.setOnClickListener(this);


        mArtwork = ((ImageView) findViewById(R.id.playback_view_artwork));
        mArtwork.setColorFilter(
                getResources().getColor(R.color.playback_view_track_artwork_filter),
                PorterDuff.Mode.SRC_ATOP
        );
        mTitle = ((TextView) findViewById(R.id.playback_view_track));
        mSeekBar = ((SeekBar) findViewById(R.id.playback_view_seekbar));
        mSeekBar.setOnSeekBarChangeListener(this);

        mSeeking = false;
    }

    private int[] getSecondMinutes(long milli) {
        int inSeconds = (int) milli / 1000;
        return new int[]{inSeconds / 60, inSeconds % 60};
    }

    /**
     * Interface used to catch player event.
     */
    public interface Listener {
        /**
         * Called when user pressed the toggle play/pause button.
         */
        void onTogglePlayPressed();

        /**
         * Called when user pressed the previous button.
         */
        void onPreviousPressed();

        /**
         * Called when user pressed the next button.
         */
        void onNextPressed();

        /**
         * Called when user touch the seek bar to request a seek to action.
         *
         * @param milli milli second to which  the player should seek to.
         */
        void onSeekToRequested(int milli);


    }
}
