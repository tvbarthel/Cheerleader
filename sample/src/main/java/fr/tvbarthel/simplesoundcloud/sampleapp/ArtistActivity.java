package fr.tvbarthel.simplesoundcloud.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
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
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.TrackView;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ArtistActivity extends ActionBarActivity implements
    PlaybackView.Listener, SimpleSoundCloudPlaylistListener {

    // bundle keys
    private static final String BUNDLE_KEY_ARTIST_NAME = "artist_activity_bundle_key_artist_name";

    // sound cloud
    private SupportSoundCloudArtistClient mSupportSoundCloudArtistClient;
    private SimpleSoundCloudPlayer mSimpleSoundCloudPlayer;

    // tracks widget
    private ProgressBar mProgress;
    private TextView mCallback;
    private RecyclerView mRetrieveTracksRecyclerView;
    private TrackView.Listener mRetrieveTracksListener;
    private ArrayList<SoundCloudTrack> mRetrievedTracks;
    private TracksAdapter mAdapter;
    private ArtistView mArtistView;

    // player widget
    private RecyclerView mPlaylistRecyclerView;
    private PlaybackView mPlaybackView;
    private TracksAdapter mPlaylistAdapter;
    private ArrayList<SoundCloudTrack> mPlaylistTracks;
    private TrackView.Listener mPlaylistTracksListener;

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


        mProgress = ((ProgressBar) findViewById(R.id.activity_artist_progress));
        mCallback = ((TextView) findViewById(R.id.activity_artist_callback));

        mRetrieveTracksRecyclerView = ((RecyclerView) findViewById(R.id.activity_artist_list));
        initRetrieveTracksRecyclerView();

        mPlaylistRecyclerView = ((RecyclerView) findViewById(R.id.activity_artist_playlist));
        initPlaylistTracksRecyclerView();
        setTrackListPadding();

        // check if tracks are already loaded into the player.
        ArrayList<SoundCloudTrack> currentsTracks = mSimpleSoundCloudPlayer.getTracks();
        if (currentsTracks != null) {
            mPlaylistTracks.addAll(currentsTracks);
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
    public void onBackPressed() {
        if (mPlaybackView.getTop() < mPlaylistRecyclerView.getHeight() - mPlaybackView.getHeight()) {
            mPlaylistRecyclerView.getLayoutManager().scrollToPosition(0);
        } else {
            super.onBackPressed();
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
        mPlaylistTracks.add(track);
        mPlaylistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTrackRemoved(SoundCloudTrack track, boolean isEmpty) {
        mPlaylistTracks.remove(track);
        mPlaylistAdapter.notifyDataSetChanged();
        if (isEmpty) {
            mPlaybackView.animate().translationY(mPlaybackView.getHeight());
        }
    }

    /**
     * Used to position the track list at the bottom of the screen.
     */
    private void setTrackListPadding() {
        mPlaylistRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPlaylistRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                int headerListHeight = getResources().getDimensionPixelOffset(R.dimen.playback_view_height);
                mPlaylistRecyclerView.setPadding(0, mPlaylistRecyclerView.getHeight() - headerListHeight, 0, 0);
                mPlaylistRecyclerView.setAdapter(mPlaylistAdapter);
                if (mPlaylistTracks.isEmpty()) {
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

    private void initRetrieveTracksRecyclerView() {
        mArtistView = new ArtistView(this);
        mRetrieveTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(SoundCloudTrack track) {
                mSimpleSoundCloudPlayer.addTrack(track);
                mPlaylistAdapter.notifyDataSetChanged();

                if (mSimpleSoundCloudPlayer.getTracks().size() == 1) {
                    mSimpleSoundCloudPlayer.play();
                }
            }
        };

        mRetrievedTracks = new ArrayList<>();
        mRetrieveTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new TracksAdapter(mRetrieveTracksListener, mRetrievedTracks);
        mAdapter.setHeaderView(mArtistView);
        mRetrieveTracksRecyclerView.setAdapter(mAdapter);
    }

    private void initPlaylistTracksRecyclerView() {

        mPlaylistTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(SoundCloudTrack track) {
                int playlistPosition = mSimpleSoundCloudPlayer.getTracks().indexOf(track);
                mSimpleSoundCloudPlayer.play(playlistPosition);
            }
        };

        mPlaybackView = new PlaybackView(this);
        mPlaybackView.setListener(this);

        mPlaylistTracks = new ArrayList<>();
        mPlaylistAdapter = new TracksAdapter(mPlaylistTracksListener, mPlaylistTracks);
        mPlaylistAdapter.setHeaderView(mPlaybackView);

        mPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
}
