package fr.tvbarthel.simplesoundcloud.sampleapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.sampleapp.R;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.TrackView;

/**
 * Simple adapter used to display tracks in a list.
 */
public class TracksAdapter extends ArrayAdapter<SoundCloudTrack> {

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

        Holder viewHolder = ((Holder) convertView.getTag());
        viewHolder.mTrackView.setModel(getItem(position));

        return convertView;
    }

    /**
     * View holder pattern.
     */
    private class Holder {
        private TrackView mTrackView;
    }
}
