package fr.tvbarthel.simplesoundcloud.sampleapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.helpers.SoundCloudArtworkHelper;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayerListener;
import fr.tvbarthel.simplesoundcloud.sampleapp.R;

/**
 * Simple view used to display basic player button : play/pause, next and previous.
 */
public class PlaybackView extends FrameLayout implements View.OnClickListener, SimpleSoundCloudPlayerListener {

    private ImageView mArtwork;
    private TextView mTitle;
    private ImageView mPlayPause;

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

    @Override
    public void onPlayerPlay(SoundCloudTrack track) {
        setTrack(track);
    }

    @Override
    public void onPlayerPause() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onPlayerSeekTo(int milli) {

    }

    @Override
    public void onPlayerDestroyed() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
        mArtwork.setImageDrawable(null);
        mTitle.setText("");
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
        Log.d("LARGONNE", " " + milli / 1000);
    }

    /**
     * Set the current played track.
     *
     * @param track track which is played.
     */
    public void setTrack(SoundCloudTrack track) {
        if (track == null) {
            mTitle.setText("");
            mArtwork.setImageDrawable(null);
            mPlayPause.setImageResource(R.drawable.ic_play_white);
        } else {
            Picasso.with(getContext())
                .load(SoundCloudArtworkHelper.getArtworkUrl(track, SoundCloudArtworkHelper.LARGE))
                .into(mArtwork);
            mTitle.setText(track.getArtist() + " - " + track.getTitle());
            mPlayPause.setImageResource(R.drawable.ic_pause_white);
            if (getTranslationY() != 0) {
                this.animate().translationY(0);
            }
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.playback_view, this);
        findViewById(R.id.playback_view_next).setOnClickListener(this);
        findViewById(R.id.playback_view_previous).setOnClickListener(this);
        mPlayPause = ((ImageView) findViewById(R.id.playback_view_toggle_play));
        mPlayPause.setOnClickListener(this);

        mArtwork = ((ImageView) findViewById(R.id.playback_view_artwork));
        mTitle = ((TextView) findViewById(R.id.playback_view_track));
    }

    /**
     * Interface used to catch player event.
     */
    public interface Listener {
        /**
         * Called when user pressed the toggle play/pause button.
         */
        public void onTogglePlayPressed();

        /**
         * Called when user pressed the previous button.
         */
        public void onPreviousPressed();

        /**
         * Called when user pressed the next button.
         */
        public void onNextPressed();
    }
}
