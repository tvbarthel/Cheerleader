package fr.tvbarthel.cheerleader.library.offline;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


/**
 * Provider used to encapsulate communication with the database.
 */
public class OfflinerProvider extends ContentProvider {

    /**
     * base uri.
     */
    public static final String CONTENT = "content://";

    /**
     * Slash.
     */
    public static final String SLASH = "/";

    private static final String UNKNOWN_URI = "Unknown URI ";
    private static final String AUTHORITY = ".Cheerleader.OfflineProvider";
    private static final int CACHE = 1;
    private static final String TAG = OfflinerProvider.class.getSimpleName();
    private static UriMatcher sUriMatcher;


    private String mAuthority;
    private OfflinerDBHelper mDbHelper;


    /**
     * Retrieve the authority of the provider.
     *
     * @param packageName package name.
     * @return authority.
     */
    public static String getAuthority(String packageName) {
        return packageName + AUTHORITY;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        try {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int count;
            switch (sUriMatcher.match(uri)) {
                case CACHE:
                    count = db.delete(OfflinerDBHelper.TABLE_CACHE, where, whereArgs);
                    break;
                default:
                    throw new IllegalArgumentException(UNKNOWN_URI + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;

    }

    @Override
    public String getType(Uri uri) {
        final String type;
        switch (sUriMatcher.match(uri)) {
            case CACHE:
                type = OfflinerDBHelper.TABLE_CACHE;
                break;
            default:
                throw new IllegalArgumentException(UNKNOWN_URI + uri);
        }

        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        try {
            ContentValues values;
            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String table;
            switch (sUriMatcher.match(uri)) {
                case CACHE:
                    table = OfflinerDBHelper.TABLE_CACHE;
                    break;
                default:
                    throw new IllegalArgumentException(UNKNOWN_URI + uri);
            }

            long rowId = db.replace(table, null, values);
            if (rowId > 0) {
                Uri noteUri = ContentUris.withAppendedId(
                        Uri.parse(CONTENT + mAuthority + SLASH + table), rowId);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new OfflinerDBHelper(getContext());
        mAuthority = getAuthority(getContext().getPackageName());
        if (sUriMatcher == null) {
            sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            sUriMatcher.addURI(mAuthority, OfflinerDBHelper.TABLE_CACHE, CACHE);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

            switch (sUriMatcher.match(uri)) {
                case CACHE:
                    qb.setTables(OfflinerDBHelper.TABLE_CACHE);
                    break;
                default:
                    throw new IllegalArgumentException(UNKNOWN_URI + uri);
            }

            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int count;
            switch (sUriMatcher.match(uri)) {
                case CACHE:
                    count = db.update(OfflinerDBHelper.TABLE_CACHE, values, where, whereArgs);
                    break;
                default:
                    throw new IllegalArgumentException(UNKNOWN_URI + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }

}
