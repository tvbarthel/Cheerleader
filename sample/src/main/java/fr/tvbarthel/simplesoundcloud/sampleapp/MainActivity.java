package fr.tvbarthel.simplesoundcloud.sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import fr.tvbarthel.simplesoundcloud.library.SimpleSoundCloud;
import fr.tvbarthel.simplesoundcloud.library.models.SoundCloudTrack;
import fr.tvbarthel.simplesoundcloud.library.player.SimpleSoundCloudListener;
import fr.tvbarthel.simplesoundcloud.sampleapp.ui.SimpleSoundCloudUserView;
import rx.functions.Action1;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    /**
     * View used to render retrieved user.
     */
    private SimpleSoundCloudUserView mSimpleSoundCloudUserView;

    private SimpleSoundCloud mSimpleSoundCloud;

    private SimpleSoundCloudListener mPlayerListener;

    private Action1<SoundCloudTrack> mAddTrack;
    private Action1<SoundCloudTrack> mPlayTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListener();

        mSimpleSoundCloudUserView = ((SimpleSoundCloudUserView) findViewById(R.id.activity_main_user));

        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.remove).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.previous).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.seek_to).setOnClickListener(this);


        mSimpleSoundCloud = new SimpleSoundCloud.Builder()
                .from(this)
                .with("57c98089f4b03928a0a611f93223a9de")
//                .notificationIcon(R.drawable.ic_launcher)
//                .notificationIconBackground(R.drawable.background)
                .notificationActivity(this)
                .log(SimpleSoundCloud.LOG_OFFLINER | SimpleSoundCloud.LOG_RETROFIT)
                .build();

//        mSimpleSoundCloud.play();
//        mSimpleSoundCloud.pause();
//        mSimpleSoundCloud.next();
//        mSimpleSoundCloud.previous();
//        mSimpleSoundCloud.seekTo(123456);
//        mSimpleSoundCloud.addTrack(172332780, false);
//        mSimpleSoundCloud.removeTrack(172332780);

        mAddTrack = new Action1<SoundCloudTrack>() {
            @Override
            public void call(SoundCloudTrack soundCloudTrack) {
                Log.d("LARGONNE", "mAddTrack : " + soundCloudTrack);
                mSimpleSoundCloud.addTrack(soundCloudTrack);
            }
        };

        mPlayTrack = new Action1<SoundCloudTrack>() {
            @Override
            public void call(SoundCloudTrack soundCloudTrack) {
                Log.d("LARGONNE", "mPlayTrack : " + soundCloudTrack);
                mSimpleSoundCloud.addTrack(soundCloudTrack);
            }
        };

        mSimpleSoundCloud.getTrack(180629660).subscribe(mAddTrack);
        mSimpleSoundCloud.getTrack(172332780).subscribe(mAddTrack);
        mSimpleSoundCloud.getTrack(179351180).subscribe(mAddTrack);

//        mSimpleSoundCloud.getUser(11621510)
//                .subscribe(new Action1<SoundCloudUser>() {
//                    @Override
//                    public void call(SoundCloudUser user) {
//                        mSimpleSoundCloudUserView.setSoundCloudUser(user);
//                    }
//                });


        new Intent(getApplicationContext(), MainActivity.class);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSimpleSoundCloud.registerPlayerListener(mPlayerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSimpleSoundCloud.unregisterPlayerListener(mPlayerListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSimpleSoundCloud.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                mSimpleSoundCloud.play();
                break;
            case R.id.pause:
                mSimpleSoundCloud.pause();
                break;
            case R.id.add:
                mSimpleSoundCloud.getTrack(180629660).subscribe(mAddTrack);
                break;
            case R.id.remove:
                mSimpleSoundCloud.removeTrack(0);
                break;
            case R.id.next:
                mSimpleSoundCloud.next();
                break;
            case R.id.previous:
                mSimpleSoundCloud.previous();
                break;
            case R.id.seek_to:
                mSimpleSoundCloud.seekTo(30000);
                break;
            case R.id.close:
                mSimpleSoundCloud.close();
                break;
        }
    }

    private void initListener() {
        mPlayerListener = new SimpleSoundCloudListener() {

            @Override
            protected void onPlay(SoundCloudTrack track) {
                super.onPlay(track);
                Log.d("LARGONNE", "onPlay : " + track);
            }

            @Override
            protected void onPause() {
                super.onPause();
                Log.d("LARGONNE", "onPause : ");
            }

            @Override
            protected void onSeekTo(int milli) {
                super.onSeekTo(milli);
                Log.d("LARGONNE", "onSeekTo : " + milli);
            }
        };
    }
}
