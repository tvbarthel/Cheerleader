package fr.tvbarthel.cheerleader.library.offline;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;

/**
 * Encapsulate offline access and storage through {@link android.content.ContentResolver}.
 * <p/>
 * Use {@link android.content.AsyncQueryHandler} to make asynchronous saving :
 * {@link fr.tvbarthel.cheerleader.library.offline.OfflinerQueryHandler#put(String, String)}
 * <p/>
 * Retrieving is synchronous :
 * {@link fr.tvbarthel.cheerleader.library.offline.OfflinerQueryHandler#put(String, String)}
 */
final class OfflinerQueryHandler extends AsyncQueryHandler {

    /**
     * Log cat.
     */
    private static final String TAG = OfflinerQueryHandler.class.getSimpleName();

    /**
     * Token to identify queries used to know if the request as already been saved for offline usage.
     */
    private static final int TOKEN_CHECK_SAVED_STATUS = 0;

    /**
     * Token to identify queries used to update json body of a request which as already been saved.
     */
    private static final int TOKEN_UPDATE_ALREADY_SAVED_REQUEST = 1;

    /**
     * Token to identify queries used to save the json body of a new request for offline access.
     */
    private static final int TOKEN_SAVE_NEW_REQUEST = 2;

    /**
     * package name of the application currently using cheerleader.
     */
    private final String mPackageName;

    /**
     * Used to know if log are enable or not.
     */
    private boolean mDebug;


    /**
     * Handle accessing and retrieving for offline storage.
     *
     * @param context holding context used to initialize internal component.
     */
    public OfflinerQueryHandler(Context context) {
        super(context.getContentResolver());
        this.mDebug = false;
        this.mPackageName = context.getPackageName();
    }

    /**
     * Enable or disable log.
     *
     * @param enable true to enable log.
     */
    public void debug(boolean enable) {
        mDebug = enable;
    }


    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);

        if (token == TOKEN_CHECK_SAVED_STATUS) {
            ContentValues values = ((ContentValues) cookie);

            if (cursor != null && cursor.getCount() != 0) {

                String url = values.getAsString(OfflinerDBHelper.REQUEST_URL);

                // request already saved for offline, update associated json.
                this.startUpdate(
                        TOKEN_UPDATE_ALREADY_SAVED_REQUEST,
                        null,
                        getUri(OfflinerDBHelper.TABLE_CACHE),
                        values,
                        OfflinerDBHelper.REQUEST_URL + " = '" + url + "'", null);

                if (mDebug) {
                    Log.d(TAG, "---> ASYNC UPDATE FOR OFFLINE ");
                }
            } else {

                // request never saved, add associated json body for offline usage.
                this.startInsert(
                        TOKEN_SAVE_NEW_REQUEST,
                        null,
                        getUri(OfflinerDBHelper.TABLE_CACHE),
                        values
                );

                if (mDebug) {
                    Log.d(TAG, "---> ASYNC INSERT FOR OFFLINE ");
                }
            }
        }

        if (mDebug) {
            Log.d(TAG, "Key : " + ((ContentValues) cookie).getAsString(OfflinerDBHelper.REQUEST_URL));
            Log.d(TAG, "Value : " + ((ContentValues) cookie).getAsString(OfflinerDBHelper.REQUEST_RESULT));
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        if (mDebug && token == TOKEN_SAVE_NEW_REQUEST) {
            Log.d(TAG, "<--- ASYNC INSERT FOR OFFLINE");
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
        if (mDebug && token == TOKEN_UPDATE_ALREADY_SAVED_REQUEST) {
            Log.d(TAG, "<--- ASYNC UPDATE FOR OFFLINE");
        }
    }

    /**
     * Save a result for offline access.
     *
     * @param url    key.
     * @param result value.
     */
    public void put(String url, String result) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(OfflinerDBHelper.REQUEST_RESULT, result);
        contentValues.put(OfflinerDBHelper.REQUEST_URL, url);
        contentValues.put(OfflinerDBHelper.REQUEST_TIMESTAMP, Calendar.getInstance().getTime().getTime());

        this.startQuery(
                TOKEN_CHECK_SAVED_STATUS,
                contentValues,
                getUri(OfflinerDBHelper.TABLE_CACHE),
                OfflinerDBHelper.PARAMS_CACHE,
                OfflinerDBHelper.REQUEST_URL + " = '" + url + "'",
                null,
                null
        );
    }

    /**
     * Retrieve a value saved for offline access.
     *
     * @param context context used to retrieve the content resolver.
     * @param url     key.
     * @return retrieved value or null if no entry match the given key.
     */
    public String get(Context context, String url) {
        final Cursor cursor = context.getContentResolver().query(getUri(OfflinerDBHelper.TABLE_CACHE),
                OfflinerDBHelper.PARAMS_CACHE, OfflinerDBHelper.REQUEST_URL
                        + " = '" + url + "'", null, null);
        String result = null;

        if (cursor != null) {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                result = cursor.getString(cursor.getColumnIndex(OfflinerDBHelper.REQUEST_RESULT));
            }
            cursor.close();
        }

        return result;
    }

    /**
     * Retrieve the URI with Cache database provider authority.
     *
     * @param string ends of the URI.
     * @return full build URI.
     */
    private Uri getUri(String string) {
        return Uri.parse(OfflinerProvider.CONTENT + OfflinerProvider.getAuthority(mPackageName)
                + OfflinerProvider.SLASH + string);
    }
}

