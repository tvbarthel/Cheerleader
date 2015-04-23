package fr.tvbarthel.simplesoundcloud.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.client.SoundCloudUser;
import fr.tvbarthel.simplesoundcloud.library.client.SupportSoundCloudArtistClient;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlayer;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudPlaylistListener;
import fr.tvbarthel.simplesoundcloud.sampleapp.adapter.TracksAdapter;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.ArtistView;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.PlaybackView;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ArtistActivity extends ActionBarActivity implements
    AdapterView.OnItemClickListener, PlaybackView.Listener, SimpleSoundCloudPlaylistListener {

    // bundle keys
    private static final String BUNDLE_KEY_ARTIST_NAME = "artist_activity_bundle_key_artist_name";

    // sound cloud
    private SupportSoundCloudArtistClient mSupportSoundCloudArtistClient;
    private SimpleSoundCloudPlayer mSimpleSoundCloudPlayer;
    private ArrayList<SoundCloudTrack> mTracks;

    // tracks widget
    private ProgressBar mProgress;
    private TextView mCallback;
    private ListView mTrackListView;
    private ArtistView mArtistView;
    private ArrayList<SoundCloudTrack> mRetrievedTracks;
    private TracksAdapter mAdapter;

    // player widget
    private ListView mPlaylistListView;
    private PlaybackView mPlaybackView;
    private TracksAdapter mPlaylistAdapter;

    /**
     * Start an ArtistActivity for a given artist name.
     * Start activity pattern.
     *
     * @param context    context used to start the activity.
     * @param artistName name of the artist.
     */
    public static void startActivity(Context context, String artistName) {
        Intent i = new Intent(context, ArtistActivity.class);
        i.putExtra(BUNDLE_KEY_ARTIST_NAME, artistName);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        String artistName = getExtraArtistName();

        mSupportSoundCloudArtistClient = new SupportSoundCloudArtistClient.Builder()
            .from(this)
            .with(R.string.sound_cloud_client_id)
            .supports(artistName)
            .build();

        mSimpleSoundCloudPlayer = new SimpleSoundCloudPlayer.Builder()
            .from(this)
            .with(R.string.sound_cloud_client_id)
            .notificationActivity(this)
            .notificationIcon(R.drawable.ic_notification)
            .build();


        mTrackListView = ((ListView) findViewById(R.id.activity_artist_list));
        mProgress = ((ProgressBar) findViewById(R.id.activity_artist_progress));
        mCallback = ((TextView) findViewById(R.id.activity_artist_callback));
        mPlaylistListView = ((ListView) findViewById(R.id.activity_artist_playlist));

        mTrackListView.setOnItemClickListener(this);

        mRetrievedTracks = new ArrayList<>();
        mAdapter = new TracksAdapter(this, mRetrievedTracks);
        mTrackListView.setAdapter(mAdapter);
        mArtistView = new ArtistView(this);
        mTrackListView.addHeaderView(mArtistView);

        mPlaybackView = new PlaybackView(this);
        mPlaybackView.setListener(this);
        mPlaylistListView.addHeaderView(mPlaybackView);

        mTracks = new ArrayList<>();
        mPlaylistAdapter = new TracksAdapter(this, mTracks);

        setTrackListPadding();

        // check if tracks are already loaded into the player.
        ArrayList<SoundCloudTrack> currentsTracks = mSimpleSoundCloudPlayer.getTracks();
        if (currentsTracks != null) {
            mTracks.addAll(currentsTracks);
        }

        // synchronize the player view with the current player (loaded track, playing state, etc.)
        mPlaybackView.synchronize(mSimpleSoundCloudPlayer);

        getArtistData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleSoundCloudPlayer.registerPlayerListener(mPlaybackView);
        mSimpleSoundCloudPlayer.registerPlayerListener(mPlaylistAdapter);
        mSimpleSoundCloudPlayer.registerPlaylistListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSimpleSoundCloudPlayer.unregisterPlayerListener(mPlaybackView);
        mSimpleSoundCloudPlayer.unregisterPlayerListener(mPlaylistAdapter);
        mSimpleSoundCloudPlayer.unregisterPlaylistListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSupportSoundCloudArtistClient.close();
        mSimpleSoundCloudPlayer.destroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == mTrackListView) {
            int adapterPosition = position - mTrackListView.getHeaderViewsCount();
            mSimpleSoundCloudPlayer.addTrack(mAdapter.getItem(adapterPosition));
            mPlaylistAdapter.notifyDataSetChanged();

            if (mSimpleSoundCloudPlayer.getTracks().size() == 1) {
                mSimpleSoundCloudPlayer.play();
            }

        } else if (parent == mPlaylistListView) {
            int trackPosition = position - mPlaylistListView.getHeaderViewsCount();
            mSimpleSoundCloudPlayer.play(trackPosition);
        }
    }

    @Override
    public void onTogglePlayPressed() {
        mSimpleSoundCloudPlayer.togglePlayback();
    }

    @Override
    public void onPreviousPressed() {
        mSimpleSoundCloudPlayer.previous();
    }

    @Override
    public void onNextPressed() {
        mSimpleSoundCloudPlayer.next();
    }

    @Override
    public void onTrackAdded(SoundCloudTrack track) {
        mTracks.add(track);
        mPlaylistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTrackRemoved(SoundCloudTrack track, boolean isEmpty) {
        mTracks.remove(track);
        mPlaylistAdapter.notifyDataSetChanged();
        if (isEmpty) {
            mPlaybackView.animate().translationY(mPlaybackView.getHeight());
        }
    }

    /**
     * Used to position the track list at the bottom of the screen.
     */
    private void setTrackListPadding() {
        mPlaylistListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPlaylistListView.getViewTreeObserver().removeOnPreDrawListener(this);
                int headerListHeight = getResources().getDimensionPixelOffset(R.dimen.playback_view_height);
                mPlaylistListView.setPadding(0, mPlaylistListView.getHeight() - headerListHeight, 0, 0);
                mPlaylistListView.setAdapter(mPlaylistAdapter);
                mPlaylistListView.setOnItemClickListener(ArtistActivity.this);
                if (mTracks.isEmpty()) {
                    mPlaybackView.setTranslationY(headerListHeight);
                }
                return true;
            }
        });
    }

    private Action1<SoundCloudUser> displayArtist() {
        return new Action1<SoundCloudUser>() {
            @Override
            public void call(SoundCloudUser soundCloudUser) {
                mArtistView.setModel(soundCloudUser);
            }
        };
    }

    private Action1<ArrayList<SoundCloudTrack>> displayTracks() {
        return new Action1<ArrayList<SoundCloudTrack>>() {
            @Override
            public void call(ArrayList<SoundCloudTrack> soundCloudTracks) {
                mProgress.setVisibility(View.INVISIBLE);
                if (soundCloudTracks.size() == 0) {
                    mCallback.setVisibility(View.VISIBLE);
                } else {
                    mRetrievedTracks.clear();
                    mRetrievedTracks.addAll(soundCloudTracks);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    private Action1<Throwable> displayCallbacks() {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mProgress.setVisibility(View.INVISIBLE);
                mCallback.setVisibility(View.VISIBLE);
            }
        };
    }

    /**
     * Used to retrieved the tracks of the artist as well as artist details.
     */
    private void getArtistData() {
        mProgress.setVisibility(View.VISIBLE);
        mCallback.setVisibility(View.INVISIBLE);
        mRetrievedTracks.clear();
        mAdapter.notifyDataSetChanged();

        AndroidObservable.bindActivity(this,
            mSupportSoundCloudArtistClient.getArtistTracks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(displayCallbacks()))
            .subscribe(displayTracks());

        AndroidObservable.bindActivity(this,
            mSupportSoundCloudArtistClient.getArtistProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()))
            .subscribe(displayArtist());
    }

    /**
     * Used to retrieve the artist name for the bundle.
     *
     * @return artist name.
     */
    private String getExtraArtistName() {
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(BUNDLE_KEY_ARTIST_NAME)) {
            throw new IllegalStateException("No artist name found, please use the startActivity pattern");
        }
        return extras.getString(BUNDLE_KEY_ARTIST_NAME);
    }
}
