package fr.tvbarthel.cheerleader.library.client;


import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Interface used to access the SoundCloud API.
 */
interface RetrofitService {

    /**
     * Retrieve a SoundCloud user profile.
     *
     * @param user SoundCloud user id as string or user name.
     * @return {@link rx.Observable}
     */
    @GET("/users/{user}.json")
    Observable<String> getUser(@Path("user") String user);

    /**
     * Retrieve all public tracks of a user.
     *
     * @param user SoundCloud user id as string or user name.
     * @return {@link rx.Observable}
     */
    @GET("/users/{user}/tracks.json")
    Observable<String> getUserTracks(@Path("user") String user);

    /**
     * Retrieve a SoundCloud track.
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable}
     */
    @GET("/tracks/{trackId}.json")
    Observable<String> getTrack(@Path("trackId") int trackId);

    /**
     * Retrieve the list of comments related to the
     *
     * @param trackId SoundCloud track id.
     * @return {@link rx.Observable}
     */
    @GET("/tracks/{trackId}/comments.json")
    Observable<String> getTrackComments(@Path("trackId") int trackId);
}
