package fr.tvbarthel.cheerleader.library.client;


import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * {@link Interceptor used to sign each call for the SoundCloud api.
 */
class RequestSignatorInterceptor implements Interceptor {

    /**
     * Query param used to sign each request.
     */
    private static final String QUERY_PARAM_CLIENT_ID = "client_id";

    /**
     * Client id used to sign each request.
     */
    private String mClientId;

    /**
     * {@link Interceptor} used to sign each request for SoundCloud http api.
     *
     * @param clientId client id.
     */
    public RequestSignatorInterceptor(String clientId) {
        setClientId(clientId);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        HttpUrl httpUrl = originalRequest
                .url()
                .newBuilder()
                .addQueryParameter(QUERY_PARAM_CLIENT_ID, mClientId)
                .build();

        return chain.proceed(originalRequest.newBuilder().url(httpUrl).build());
    }

    /**
     * Set the client id used to sign each request.
     *
     * @param clientId SoundCloud client id.
     */
    void setClientId(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client id can't be null");
        }
        mClientId = clientId;
    }
}
