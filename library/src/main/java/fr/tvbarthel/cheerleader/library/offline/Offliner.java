package fr.tvbarthel.cheerleader.library.offline;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Allow to save Response body for offline usage.
 * <p/>
 * Saved response body could then be retrieveFromCache for offline usage.
 */
public final class Offliner {

    /**
     * Tag for log cat.
     */
    private static final String TAG = Offliner.class.getSimpleName();

    /**
     * Query handler used to encapsulate offline access and storage through {@link android.content.ContentResolver}
     */
    private OfflinerQueryHandler mCacheQueryHandler;

    /**
     * Enable/Disable log.
     */
    private boolean mDebug;

    /**
     * Week reference on context used to access to the content resolver.
     */
    private WeakReference<Context> mContext;

    /**
     * OkHttp interceptor used to intercept response in order to save or retrieve the content
     * from local offline storage.
     */
    private Interceptor mInternalInterceptor;

    /**
     * Private constructor to avoid concurrent access.
     *
     * @param context context used to instantiate internal component.
     * @param debug   true to enable debug log.
     */
    public Offliner(Context context, boolean debug) {
        super();
        mDebug = debug;
        mCacheQueryHandler = new OfflinerQueryHandler(context.getContentResolver());
        mContext = new WeakReference<>(context.getApplicationContext());
        mCacheQueryHandler.debug(debug);
        mInternalInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response;
                try {
                    response = chain.proceed(request);
                } catch (IOException e) {
                    return retrieveResponseFromCache(request);
                }

                // sync cache retrieving if needed.
                if (response.code() == 404) {
                    Response cachedResponse = retrieveResponseFromCache(request, response);
                    if (cachedResponse != null) {
                        response = cachedResponse;
                    }
                } else {
                    // async save for further use
                    response = save(response);
                }
                return response;
            }
        };
    }

    /**
     * Retrieve the {@link Interceptor} used to perform offline saving/retrieving.
     *
     * @return the {@link Interceptor} used to perform offline saving/retrieving.
     */
    public Interceptor getInterceptor() {
        return mInternalInterceptor;
    }

    /**
     * Enable or disable log for the current instance.
     *
     * @param enable true to enable log.
     */
    public void debug(boolean enable) {
        mDebug = enable;
        mCacheQueryHandler.debug(enable);
    }

    /**
     * Retrieve a {@link Response} from the offline layer.
     *
     * @param request request for which an offline response must be retrieved
     * @return offline response or null if no response can be retrieve from the offline layer.
     */
    private Response retrieveResponseFromCache(Request request) {
        return retrieveResponseFromCache(request, null);
    }

    /**
     * Retrieve a {@link Response} from the offline layer.
     *
     * @param request  request for which an offline response must be retrieved
     * @param response original response which lead to an offline access. Can be null.
     * @return offline response or null if no response can be retrieve from the offline layer.
     */
    private Response retrieveResponseFromCache(Request request, @Nullable Response response) {
        Response.Builder cachedResponse;
        if (response != null) {
            cachedResponse = response.newBuilder();
        } else {
            cachedResponse = new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1);
        }
        String cached = retrieveFromCache(request.url().toString());
        if (cached != null) {
            return cachedResponse
                    .code(200)
                    .body(ResponseBody.create(
                                    MediaType.parse("application/json"),
                                    cached)
                    ).build();
        } else {
            return response;
        }
    }

    /**
     * Get a saved body for a given url.
     *
     * @param url url for which we need to retrieve the response body in offline mode.
     * @return response body as string.
     */
    private String retrieveFromCache(String url) {
        String savedJson;

        savedJson = mCacheQueryHandler.get(getContext(), url);
        log("---------- body found in offline saver : " + savedJson);
        log("----- NO NETWORK : retrieving ends");
        return savedJson;
    }

    /**
     * Log in LogCat when debug mode is enable.
     *
     * @param message text to be logged.
     */
    private void log(String message) {
        if (mDebug) {
            Log.d(TAG, message);
        }
    }

    /**
     * Retrieve the context if not yet garbage collected.
     *
     * @return context.
     */
    private Context getContext() {
        Context context = mContext.get();
        if (context == null) {
            throw new IllegalStateException("Context used by the instance has been destroyed.");
        }
        return context;
    }

    /**
     * Allow to save the Response body for offline usage.
     *
     * @param response response to save
     */
    private Response save(Response response) {
        String jsonBody;
        String key = response.request().url().toString();

        log("----- SAVE FOR OFFLINE : saving starts");
        log("---------- for request : " + key);
        log("---------- trying to parse response body");

        //Try to get response body
        BufferedReader reader;
        StringBuilder sb = new StringBuilder();
        log("---------- trying to parse response body");
        InputStream bodyStream = response.body().byteStream();
        InputStreamReader bodyReader = new InputStreamReader(bodyStream, Charset.forName("UTF-8"));
        reader = new BufferedReader(bodyReader);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            bodyReader.close();
            bodyReader.close();
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e.getMessage());
        }


        jsonBody = sb.toString();


        log("---------- trying to save response body for offline");

        mCacheQueryHandler.put(key, jsonBody);

        log("---------- url : " + key);
        log("---------- body : " + jsonBody);
        log("----- SAVE FOR OFFLINE : saving ends");

        return response.newBuilder().body(ResponseBody.create(response.body().contentType(), jsonBody)).build();
    }
}
