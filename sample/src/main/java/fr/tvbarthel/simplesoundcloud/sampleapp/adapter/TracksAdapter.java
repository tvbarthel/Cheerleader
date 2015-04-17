package fr.tvbarthel.simplesoundcloud.sampleapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayerListener;
import fr.tvbarthel.simplesoundcloud.sampleapp.R;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.TrackView;

/**
 * Simple adapter used to display tracks in a list.
 */
public class TracksAdapter extends ArrayAdapter<SoundCloudTrack> implements SimpleSoundCloudPlayerListener {


    /**
     * Current played track used to display an indicator.
     */
    private SoundCloudTrack mPlayedTrack;

    /**
     * Simple adapter used to display tracks in a list.
     *
     * @param context holding context.
     * @param tracks  tracks.
     */
    public TracksAdapter(Context context, List<SoundCloudTrack> tracks) {
        super(context, R.layout.track_view, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            TrackView rawView = new TrackView(getContext());
            Holder holder = new Holder();
            holder.mTrackView = rawView;

            convertView = rawView;
            convertView.setTag(holder);
        }

        SoundCloudTrack track = getItem(position);
        Holder viewHolder = ((Holder) convertView.getTag());
        viewHolder.mTrackView.setModel(track);

        if (track.equals(mPlayedTrack)) {
            viewHolder.mTrackView.setBackgroundResource(R.drawable.selectable_background_grey);
        } else {
            viewHolder.mTrackView.setBackgroundResource(R.drawable.selectable_background_white);
        }

        return convertView;
    }


    ////////////////////////////////////////////////////////////
    ///// Player listener used to keep played track updated ////
    ////////////////////////////////////////////////////////////

    @Override
    public void onPlayerPlay(SoundCloudTrack track) {
        mPlayedTrack = track;
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
     * View holder pattern.
     */
    private class Holder {
        private TrackView mTrackView;
    }
}
