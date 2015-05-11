package fr.tvbarthel.cheerleader.sampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import fr.tvbarthel.cheerleader.library.client.SoundCloudTrack;
import fr.tvbarthel.cheerleader.library.client.SoundCloudUser;
import fr.tvbarthel.cheerleader.library.client.SupportSoundCloudArtistClient;
import fr.tvbarthel.cheerleader.library.player.SimpleSoundCloudPlayer;
import fr.tvbarthel.cheerleader.library.player.SimpleSoundCloudPlaylistListener;
import fr.tvbarthel.cheerleader.sampleapp.adapter.TracksAdapter;
import fr.tvbarthel.cheerleader.sampleapp.ui.ArtistView;
import fr.tvbarthel.cheerleader.sampleapp.ui.CroutonView;
import fr.tvbarthel.cheerleader.sampleapp.ui.PlaybackView;
import fr.tvbarthel.cheerleader.sampleapp.ui.TrackView;
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
    private RecyclerView.OnScrollListener mRetrieveTracksScrollListener;
    private int mScrollY;

    // player widget
    private RecyclerView mPlaylistRecyclerView;
    private PlaybackView mPlaybackView;
    private TracksAdapter mPlaylistAdapter;
    private ArrayList<SoundCloudTrack> mPlaylistTracks;
    private TrackView.Listener mPlaylistTracksListener;

    // banner
    private View mBanner;

    //Crouton, contextual toast.
    private Crouton mCrouton;
    private CroutonView mCroutonView;

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
        mBanner = findViewById(R.id.activity_artist_banner);

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
            mPlaylistRecyclerView.getLayoutManager().smoothScrollToPosition(mPlaylistRecyclerView, null, 0);
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
            return ""; // activity started through the notification pending intent
        }
        return extras.getString(BUNDLE_KEY_ARTIST_NAME);
    }

    private void initRetrieveTracksRecyclerView() {
        mArtistView = new ArtistView(this);
        mRetrieveTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(SoundCloudTrack track) {
                boolean playNow = !mSimpleSoundCloudPlayer.isPlaying();

                mSimpleSoundCloudPlayer.addTrack(track, playNow);
                mPlaylistAdapter.notifyDataSetChanged();

                if (!playNow) {
                    toast(R.string.toast_track_added);
                }
            }
        };

        mRetrievedTracks = new ArrayList<>();
        mRetrieveTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new TracksAdapter(mRetrieveTracksListener, mRetrievedTracks);
        mAdapter.setHeaderView(mArtistView);
        mRetrieveTracksRecyclerView.setAdapter(mAdapter);

        mScrollY = 0;
        mRetrieveTracksScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollY += dy;
                mBanner.setTranslationY(-mScrollY / 2f);
            }
        };
        mRetrieveTracksRecyclerView.setOnScrollListener(mRetrieveTracksScrollListener);
    }

    private void initPlaylistTracksRecyclerView() {

        mPlaylistTracksListener = new TrackView.Listener() {
            @Override
            public void onTrackClicked(SoundCloudTrack track) {
                mSimpleSoundCloudPlayer.play(track);
            }
        };

        mPlaybackView = new PlaybackView(this);
        mPlaybackView.setListener(this);

        mPlaylistTracks = new ArrayList<>();
        mPlaylistAdapter = new TracksAdapter(mPlaylistTracksListener, mPlaylistTracks);
        mPlaylistAdapter.setHeaderView(mPlaybackView);

        mPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    /**
     * Used to display crouton toast.
     *
     * @param message text to be displayed.
     */
    private void toast(@StringRes int message) {
        if (mCrouton != null) {
            mCrouton.cancel();
            mCrouton = null;
        }
        mCroutonView = new CroutonView(this, getString(message));

        mCrouton = Crouton.make(this, mCroutonView, R.id.activity_artist_main_container);
        mCrouton.show();
    }
}
