package fr.tvbarthel.simplesoundcloud.library;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Interface used to access the SoundCloud API.
 */
interface SimpleSoundCloudService {

    /**
     * Retrieve a SoundCloud user profile.
     *
     * @param userId SoundCloud user id.
     * @return {@link rx.Observable} on {@link com.google.gson.JsonObject}
     */
    @GET("/users/{userId}.json")
    public Observable<Response> getUser(@Path("userId") int userId);
}
