package fr.tvbarthel.cheerleader.library.offline;

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
public final class Offliner {

    /**
     * Prepare an {@link rx.Observable} of {@link retrofit.client.Response} for offline usage.
     * <p/>
     * This will save JSON body or retrieved it for offline usage.
     * <p/>
     * {@link Offliner#initInstance(android.content.Context, boolean)}
     * must have been called before.
     */
    public static final Observable.Transformer<Response, String> PREPARE_FOR_OFFLINE
            = new Observable.Transformer<Response, String>() {
        @Override
        public Observable<String> call(Observable<Response> observable) {
            return observable.map(getInstance().save)
                    .onErrorReturn(getInstance().retrieve);
        }
    };

    /**
     * Tag for log cat.
     */
    private static final String TAG = Offliner.class.getSimpleName();

    /**
     * Instance.
     */
    private static Offliner sInstance;

    /**
     * Used to encapsulate offline access and storage through {@link android.content.ContentResolver}.
     */
    private OfflinerQueryHandler mOfflinerQueryHandler;


    /**
     * Allow an {@link rx.Observable<retrofit.client.Response>} to save the Response body
     * for offline usage.
     */
    private Func1<Response, String> save = new Func1<Response, String>() {
        @Override
        public String call(Response response) {
            String jsonBody;

            long start = System.currentTimeMillis();
            log("---> SAVE FOR OFFLINE");
            log("Request : " + response.getUrl());

            //Try to get response body
            BufferedReader reader;
            StringBuilder sb = new StringBuilder();
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
            mOfflinerQueryHandler.put(key, jsonBody);
            log("Json body saved : " + jsonBody);

            log("<--- SAVE FOR OFFLINE REQUESTED (" + (System.currentTimeMillis() - start) + "ms)");
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
     * @param applicationContext context used to retrieve  {@link android.content.ContentResolver}
     * @param debug              true to enable setLog log.
     */
    private Offliner(Context applicationContext, boolean debug) {
        this.mDebug = debug;
        this.mOfflinerQueryHandler = new OfflinerQueryHandler(applicationContext.getContentResolver());
        this.mContext = new WeakReference<>(applicationContext);
    }

    /**
     * Retrieve the static instance.
     * <p/>
     * {@link Offliner#initInstance(android.content.Context, boolean)} must have
     * been called.
     *
     * @return {@link Offliner} instance.
     */
    public static Offliner getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("initInstance must be called before get the instance");
        }
        return sInstance;
    }

    /**
     * Initialize the static instance.
     *
     * @param context context store in a {@link java.lang.ref.WeakReference} to avoid memory leak.
     * @param debug   true if setLog mode is enable.
     * @return {@link Offliner} instance.
     */
    public static Offliner initInstance(Context context, boolean debug) {
        if (sInstance == null) {
            sInstance = new Offliner(context, debug);
        } else {
            sInstance.mContext = new WeakReference<>(context);
        }

        return sInstance;
    }

    /**
     * Enable or disable log for the current instance.
     *
     * @param enable true to enable log.
     */
    public static void debug(boolean enable) {
        if (sInstance != null) {
            sInstance.mDebug = enable;
            sInstance.mOfflinerQueryHandler.debug(enable);
        }
    }

    /**
     * Get a saved body for a given url.
     *
     * @param url url for which we need to retrieve the response body in offline mode.
     * @return response body as string.
     */
    private String retrieveFromCache(String url) {
        long start = System.currentTimeMillis();
        String savedJson;
        log("---> RETRIEVE FROM OFFLINE STORAGE ");
        log("Request : " + url);
        savedJson = mOfflinerQueryHandler.get(getContext(), url);
        log("Retrieved body json : " + savedJson);
        log("<--- RETRIEVE FROM OFFLINE STORAGE (" + (System.currentTimeMillis() - start) + "ms)");
        return savedJson;
    }

    /**
     * Log in LogCat when setLog mode is enable.
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
