package fr.tvbarthel.simplesoundcloud.library;

import retrofit.RequestInterceptor;

/**
 * {@link retrofit.RequestInterceptor} used to sign each call for the SoundCloud api.
 */
class SimpleSoundCloudRequestSignator implements RequestInterceptor {

    /**
     * Query param used to sign each request.
     */
    private static final String QUERY_PARAM_CLIENT_ID = "client_id";

    /**
     * Client id used to sign each request.
     */
    private String mClientId;

    /**
     * {@link retrofit.RequestInterceptor} used to sign each request for SoundCloud http api.
     *
     * @param clientId client id.
     */
    public SimpleSoundCloudRequestSignator(String clientId) {
        setClientId(clientId);
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addEncodedQueryParam(QUERY_PARAM_CLIENT_ID, mClientId);
    }

    /**
     * Set the client id used to sign each request.
     *
     * @param clientId SoundCloud client id.
     */
    public void setClientId(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client id can't be null");
        }
        mClientId = clientId;
    }
}
