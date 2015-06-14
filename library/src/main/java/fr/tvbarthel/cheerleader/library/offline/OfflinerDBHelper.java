package fr.tvbarthel.cheerleader.library.offline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import fr.tvbarthel.cheerleader.library.BuildConfig;


/**
 * Helper used to encapsulate the creation of the database use for the offline system.
 */
class OfflinerDBHelper extends SQLiteOpenHelper {

    /**
     * table cache name.
     */
    public static final String TABLE_CACHE = "CHEERLEADER_OFFLINE";

    /**
     * Column used to store the url.
     */
    public static final String REQUEST_URL = "url";

    /**
     * Column used to store the result.
     */
    public static final String REQUEST_RESULT = "result";

    /**
     * Column used to store the timestamp of the storage.
     */
    public static final String REQUEST_TIMESTAMP = "timestamp";

    /**
     * Column of the cache table.
     */
    public static final String[] PARAMS_CACHE = new String[]{
            REQUEST_URL,
            REQUEST_RESULT,
            REQUEST_TIMESTAMP};

    /**
     * SQ query to create cache table.
     */
    public static final String CREATE_TABLE_CACHE = "CREATE TABLE " + TABLE_CACHE + "("
            + REQUEST_TIMESTAMP + " BIGINT, "
            + REQUEST_URL + " VARCHAR(255) PRIMARY KEY, "
            + REQUEST_RESULT + " TEXT);";

    private static final String DATABASENAME = "cheerleader_offline.db";
    private static final int DATABASEVERSION = 1;
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private static final String TAG = OfflinerDBHelper.class.getSimpleName();

    /**
     * Constructor.
     *
     * @param context hosting context.
     */
    public OfflinerDBHelper(Context context) {
        super(context, DATABASENAME, null, DATABASEVERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CACHE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DROP_TABLE + TABLE_CACHE);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "error while dropping table");
            }
        }
        onCreate(db);
    }
}