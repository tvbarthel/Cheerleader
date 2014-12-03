package fr.tvbarthel.simplesoundcloud.library.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Calendar;

/**
 * Helper use to encapsulate offline access and storage.
 */
final class OfflinerHelper {

    /**
     * non instantiable class.
     */
    private OfflinerHelper() {

    }

    /**
     * Retrieve the URI with Cache database provider authority.
     *
     * @param string ends of the URI.
     * @return full build URI.
     */
    public static Uri getUri(String string) {
        return Uri.parse(OfflinerProvider.CONTENT + OfflinerProvider.getAuthority()
                + OfflinerProvider.SLASH + string);
    }

    /**
     * Save a result for offline access.
     *
     * @param context context used to retrieve the content resolver.
     * @param url     key.
     * @param result  value.
     */
    public static void put(Context context, String url, String result) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(OfflinerDBHelper.REQUEST_RESULT, result);
        values.put(OfflinerDBHelper.REQUEST_URL, url);
        values.put(OfflinerDBHelper.REQUEST_TIMESTAMP, Calendar.getInstance().getTime().getTime());

        final Cursor cursor = context.getContentResolver().query(
                getUri(OfflinerDBHelper.TABLE_CACHE), OfflinerDBHelper.PARAMS_CACHE,
                OfflinerDBHelper.REQUEST_URL + " = '" + url + "'", null, null);
        if (cursor != null && cursor.getCount() != 0) {
            context.getContentResolver().update(getUri(OfflinerDBHelper.TABLE_CACHE), values,
                    OfflinerDBHelper.REQUEST_URL + " = '" + url + "'", null);
        } else {
            context.getContentResolver().insert(getUri(OfflinerDBHelper.TABLE_CACHE), values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Retrieve a value saved for offline access.
     *
     * @param context context used to retrieve the content resolver.
     * @param url     key.
     * @return retrieved value or null if no entry match the given key.
     */
    public static String get(Context context, String url) {
        final Cursor cursor = context.getContentResolver()
                .query(getUri(OfflinerDBHelper.TABLE_CACHE),
                        OfflinerDBHelper.PARAMS_CACHE, OfflinerDBHelper.REQUEST_URL
                                + " = '" + url + "'", null, null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            String result = cursor.getString(cursor.getColumnIndex(OfflinerDBHelper.REQUEST_RESULT));
            cursor.close();
            return result;
        } else {
            return null;
        }
    }
}

