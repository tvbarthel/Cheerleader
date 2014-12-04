package fr.tvbarthel.simplesoundcloud.library.offline;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;

/**
 * Allow to save Response body for offline usage.
 * <p/>
 * Saved response body could then be retrieved for offline usage.
 */
public final class SimpleSoundCloudOffliner {

    /**
     * Prepare an {@link rx.Observable} of {@link retrofit.client.Response} for offline usage.
     * <p/>
     * This will save JSON body or retrieved it for offline usage.
     * <p/>
     * {@link fr.tvbarthel.simplesoundcloud.library.offline.SimpleSoundCloudOffliner#initInstance(android.content.Context, boolean)}
     * must have been called before.
     */
    public static final Observable.Transformer<Response, String> PREPARE_FOR_OFFLINE
            = new Observable.Transformer<Response, String>() {
        @Override
        public Observable<? extends String> call(Observable<? extends Response> observable) {
            return observable.map(getInstance().save)
                    .onErrorReturn(getInstance().retrieve);
        }
    };

    /**
     * Tag for log cat.
     */
    private static final String TAG = SimpleSoundCloudOffliner.class.getSimpleName();

    /**
     * Instance.
     */
    private static SimpleSoundCloudOffliner sInstance;

    /**
     * Allow an {@link rx.Observable<retrofit.client.Response>} to save the Response body
     * for offline usage.
     */
    private Func1<Response, String> save = new Func1<Response, String>() {
        @Override
        public String call(Response response) {

            String jsonBody;

            log("----- SAVE FOR OFFLINE : saving starts");
            log("---------- for request : " + response.getUrl());


            //Try to get response body
            BufferedReader reader;
            StringBuilder sb = new StringBuilder();
            log("---------- trying to parse response body");
            try {
                reader = new BufferedReader(new InputStreamReader(response.getBody().in(),
                        Charset.forName("UTF-8")));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException : " + e.getMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.getMessage());
            }


            jsonBody = sb.toString();
            String key = response.getUrl();
            log("---------- trying to save response body for offline");
            OfflinerHelper.put(getContext(), key, jsonBody);
            log("---------- url : " + key);
            log("---------- body : " + jsonBody);

            log("----- SAVE FOR OFFLINE : saving ends");
            return jsonBody;
        }
    };

    /**
     * Allow an {@link rx.Observable<retrofit.client.Response>} to retrieveFromCache the Response body from
     * offline saver when an {@link rx.Observable#onErrorReturn(rx.functions.Func1)} is called.
     */
    private Func1<Throwable, String> retrieve = new Func1<Throwable, String>() {
        @Override
        public String call(Throwable throwable) {
            String url = ((RetrofitError) throwable).getUrl();
            return retrieveFromCache(url);
        }
    };

    /**
     * Enable/Disable log.
     */
    private boolean mDebug;

    private WeakReference<Context> mContext;

    /**
     * Private constructor to avoid concurrent access.
     *
     * @param debug true to enable debug log.
     */
    private SimpleSoundCloudOffliner(boolean debug) {
        mDebug = debug;
    }

    /**
     * Retrieve the static instance.
     * <p/>
     * {@link SimpleSoundCloudOffliner#initInstance(android.content.Context, boolean)} must have
     * been called.
     *
     * @return {@link SimpleSoundCloudOffliner} instance.
     */
    public static SimpleSoundCloudOffliner getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("initInstance must be called before get the instance");
        }
        return sInstance;
    }

    /**
     * Initialize the static instance.
     *
     * @param context context store in a {@link java.lang.ref.WeakReference} to avoid memory leak.
     * @param debug   true if debug mode is enable.
     * @return {@link SimpleSoundCloudOffliner} instance.
     */
    public static SimpleSoundCloudOffliner initInstance(Context context, boolean debug) {
        if (sInstance == null) {
            sInstance = new SimpleSoundCloudOffliner(debug);
        }
        sInstance.mContext = new WeakReference<>(context);

        return sInstance;
    }

    /**
     * Get a saved body for a given url.
     *
     * @param url url for which we need to retrieve the response body in offline mode.
     * @return response body as string.
     */
    private String retrieveFromCache(String url) {
        String savedJson;

        savedJson = OfflinerHelper.get(getContext(), url);
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
}
