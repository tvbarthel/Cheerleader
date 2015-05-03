package fr.tvbarthel.simplesoundcloud.sampleapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayerListener;
import fr.tvbarthel.simplesoundcloud.sampleapp.R;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.TrackView;

/**
 * Simple adapter used to display artist tracks in a list with an optional header.
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.Holder> implements SimpleSoundCloudPlayerListener {

    /**
     * View types.
     */
    private static final int VIEW_TYPE_TRACK = 1;
    private static final int VIEW_TYPE_HEADER = 2;

    /**
     * Current played track playlist position used to display an indicator.
     */
    private int mPlayedTrackPosition;

    /**
     * Adapted tracks.
     */
    private List<SoundCloudTrack> mTracks;

    /**
     * view header
     */
    private View mHeaderView;

    /**
     * listener used to catch event on the raw view.
     */
    private TrackView.Listener mListener;

    /**
     * Simple adapter used to display tracks in a list.
     *
     * @param listener listener used to catch event on the raw view.
     * @param tracks   tracks.
     */
    public TracksAdapter(TrackView.Listener listener, List<SoundCloudTrack> tracks) {
        super();
        mTracks = tracks;
        mPlayedTrackPosition = -1;
        mListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Holder holder;
        switch (i) {
            case VIEW_TYPE_TRACK:
                TrackView v = new TrackView(viewGroup.getContext());
                v.setListener(mListener);
                v.setLayoutParams(
                    new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                );
                holder = new TrackHolder(v);
                break;
            case VIEW_TYPE_HEADER:
                holder = new HeaderHolder(mHeaderView);
                break;
            default:
                throw new IllegalStateException("View type not handled : " + i);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int i) {
        switch (holder.viewType) {
            case VIEW_TYPE_TRACK:
                int offset = mHeaderView != null ? 1 : 0;
                ((TrackHolder) holder).trackView.setModel(mTracks.get(i - offset));
                if (i == mPlayedTrackPosition) {
                    ((TrackHolder) holder).trackView
                        .setBackgroundResource(R.drawable.selectable_background_red_light);
                } else {
                    ((TrackHolder) holder).trackView
                        .setBackgroundResource(R.drawable.selectable_background_white);
                }
                break;
            case VIEW_TYPE_HEADER:
                // do nothing
                break;
            default:
                throw new IllegalStateException("Unhandled view type : " + holder.viewType);
        }
    }

    @Override
    public int getItemCount() {
        int header = mHeaderView == null ? 0 : 1;
        return header + mTracks.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_TRACK;
        }
    }

    ////////////////////////////////////////////////////////////
    ///// Player listener used to keep played track updated ////
    ////////////////////////////////////////////////////////////

    @Override
    public void onPlayerPlay(SoundCloudTrack track, int position) {
        int offset = mHeaderView == null ? 0 : 1;
        mPlayedTrackPosition = position + offset;
        notifyDataSetChanged();
    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPlayerSeekTo(int milli) {

    }

    @Override
    public void onPlayerDestroyed() {

    }

    @Override
    public void onBufferingStarted() {

    }

    @Override
    public void onBufferingEnded() {

    }

    @Override
    public void onProgressChanged(int milli) {

    }

    /**
     * Set the header view.
     *
     * @param v header view.
     */
    public void setHeaderView(View v) {
        mHeaderView = v;
    }

    /**
     * View holder pattern.
     */
    public static abstract class Holder extends RecyclerView.ViewHolder {
        private int viewType;

        public Holder(View v, int viewType) {
            super(v);
            this.viewType = viewType;
        }
    }

    /**
     * View holder for a track view.
     */
    public static class TrackHolder extends Holder {
        private TrackView trackView;

        public TrackHolder(TrackView v) {
            super(v, VIEW_TYPE_TRACK);
            this.trackView = v;
        }
    }

    /**
     * View holder for the view header.
     */
    public static class HeaderHolder extends Holder {

        public HeaderHolder(View v) {
            super(v, VIEW_TYPE_HEADER);
        }
    }
}
