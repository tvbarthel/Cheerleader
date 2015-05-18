# Cheerleader
Cheerleader is a Android open source library implemented during our free time and designed to easily support an artist in an Android application thanks to a SoundCloud account.

The library is based on RxJava, if you aren't familiar with it the following links are strongly recommended :
* [Official wiki](https://github.com/ReactiveX/RxJava/wiki)
* [Must-read tutorials written by Dan Lew](http://blog.danlew.net/2014/09/15/grokking-rxjava-part-1/)

The library is composed of two main classes :
- the client : [CheerleaderClient](#cheerleaderclient);
- the player : [CheerleaderPlayer](#cheerleaderplayer);

# Summary
* [Gradle Dependency](#gradle-dependency)
* [CheerleaderClient](#cheerleaderclient)
* [CheerleaderPlayer](#cheerleaderplayer)

# Gradle Dependency
Waiting for upload on jcenter repositories.

# CheerleaderClient 
As any client, the CheerleaderClient will provide a bridge between your app and the data stored on the SoundCloud servers.

Severals features have been implemented to avoid too many access to the SoundCloud API as well as trying to provide a better user experience : 
- Data will be cached in RAM as long as the client isn't closed (artist and tracks data are unlikely to change every minutes).
- Response will be stored in a local database for offline usage (see "download track" feature in the TODO list for complete offline mode support).

## Builder 
```java
  mCheerleaderClient = new CheerleaderClient.Builder()
            .from(context)
            .with(R.string.sound_cloud_client_id)
            .supports("artistToSupportName")
            .build();
```
Note that only one artist can be supported at the same time, any new attempt to build a new client for another artist will automatically close the old one.

##Features
Currently only few features are available. Have a look the TODO section to check incomming features.

### Artist's profile
Once a client has been build for a given artist, the complete SoundCloud profile can be retrieved (name, avatar url, description, followers count, etc.) : 

```java
  mCheerleaderClient.getArtistProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<SoundCloudUser>() {
                @Override
                public void call(SoundCloudUser soundCloudUser) {
                    
                }
            });
```

### Artist's tracks
Public tracks of the supported artist are also available (title, artwork url, duration, wave form url, etc.) : 

```java
  mCheerleaderClient.getArtistTracks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<ArrayList<SoundCloudTrack>>() {
                @Override
                public void call(ArrayList<SoundCloudTrack> soundCloudTracks) {
                    
                }
            });
```

### Comments
In addition, comments of a given track can be retrieved : 

```java
  mCheerleaderClient.getTrackComments(track)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<ArrayList<SoundCloudComment>>() {
                @Override
                public void call(ArrayList<SoundCloudComment> soundCloudComments) {
                    
                }
            });
```

## Debug
Designed to simplify the implementation of the library, we kept the possibility to display some logs in the LogCat : 
```java
  mCheerleaderClient = new CheerleaderClient.Builder()
            .from(context)
            .with(R.string.sound_cloud_client_id)
            .log(CheerleaderClient.LOG_OFFLINER)
            .supports("artistToSupportName")
            .build();
```

# CheerleaderPlayer
The player encapsulate the whole playback logic including :
- An internal playlist;
- Notification, small and expanded (Artist name, track title, track artwork, actions -play, pause, previous, next-);
- Lock screen enhancement while playing (track artwork);
- Audio focus compliant (pause when headset is unplugged, pause/resume while hanging up the phone, etc.);
- Support of third app equalizer. 

## Builder
```java
  mCheerleaderPlayer = new CheerleaderPlayer.Builder()
            .from(this)
            .with(R.string.sound_cloud_client_id)
            .build();
```

### Notification
By default, no pending intent is attached to the notification.  In order to link your player activity just add the following lines to the Builder :
```java
  mCheerleaderPlayer = new CheerleaderPlayer.Builder()
            .from(this)
            .notificationActivity(this)
            .notificationIcon(R.drawable.ic_notification)
            .notificationIconBackground(R.drawable.notification_ic_background)
            .with(R.string.sound_cloud_client_id)
            .build();
```
Note that small icon as well as small icon background (Lollipop only) can also be customized through the Builder.

## Player interface
The public interface of the Player should allow to perform all actions expected from a player. 

```java
   mCheerleaderPlayer.addTrack(track, playNow);
   ...
   mCheerleaderPlayer.togglePlayback();
   ...
   mCheerleaderPlayer.previous();
   ...
   mCheerleaderPlayer.next();
   ...
   mCheerleaderPlayer.seekTo(milli);
   ...
   mCheerleaderPlayer.isPlaying();
   ...
   mCheerleaderPlayer.getCurrentTrack();
   ...
   
```

Check the java doc as well as the sample for more information on any variant of the methods above.

## Player Listener
In order to provide meaningfull callback to the user, two diferent kinds of listener can be registered.

### CheerleaderPlayerListener
Listener used to catch any events related to the playback including thoses from the notification / lockscreen controller.
```java
  /**
     * Called when a track starts to be played.
     *
     * @param track    played track.
     * @param position position of the played track in the playlist.
     */
    void onPlayerPlay(SoundCloudTrack track, int position);

    /**
     * Called when a the player has been paused.
     */
    void onPlayerPause();

    /**
     * Called when the player complete a seek action.
     *
     * @param milli time in milli of the seek.
     */
    void onPlayerSeekTo(int milli);

    /**
     * Called when the player has been destroyed.
     */
    void onPlayerDestroyed();

    /**
     * Called when the player paused due to buffering more data.
     */
    void onBufferingStarted();

    /**
     * Called when the player resumed due after having buffered enough data.
     */
    void onBufferingEnded();

    /**
     * Called when current position time changed.
     *
     * @param milli current time in milli seconds.
     */
    void onProgressChanged(int milli);
```

See also ```java mCheerleaderPlayer.registerPlayerListener(listener) ``` and ```java mCheerleaderPlayer.unregisterPlayerListener(listener) ```

### CheerleaderPlaylistListener
A second listener used to catch any events performed on the internal playlist

```java 
    /**
     * Called when a tracks has been added to the player playlist.
     *
     * @param track track added.
     */
    void onTrackAdded(SoundCloudTrack track);


    /**
     * Called when a tracks has been removed from the player playlist.
     *
     * @param track   track removed.
     * @param isEmpty true if the playlist is empty after deletion.
     */
    void onTrackRemoved(SoundCloudTrack track, boolean isEmpty);
```
See also ```java mCheerleaderPlayer.registerPlaylistListener(listener) ``` and ```java mCheerleaderPlayer.unregisterPlaylistListener(listener) ```






