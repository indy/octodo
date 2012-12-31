package io.indy.octodo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    // The index (key) column name for use in where clauses.
    public static final String KEY_ID = "id";

    // The name and column index of each column in your database.
    // These should be descriptive.

    // task columns
    public static final String LIST_ID = "list_id";
    public static final String STATE = "state";
    public static final String STARTED_AT = "started_at";
    public static final String FINISHED_AT = "finished_at";
    public static final String CONTENT = "content";

    // list columns
    public static final String LIST_NAME = "name";
    public static final String CREATED_AT = "created_at";

    // Database open/upgrade helper
    private ModelDBOpenHelper modelDBOpenHelper;

    public Database(Context context) {
        modelDBOpenHelper = new ModelDBOpenHelper(context,
                ModelDBOpenHelper.DATABASE_NAME, null,
                ModelDBOpenHelper.DATABASE_VERSION);

        if (D)
            Log.d(TAG, "constructor");
    }

    // Called when you no longer need access to the database.
    public void closeDatabase() {
        modelDBOpenHelper.close();
    }

    public List<String> getListNames() {
        Cursor cursor = getListNamesCursor();
        int NAME_INDEX = cursor.getColumnIndexOrThrow(LIST_NAME);

        List<String> res = new ArrayList<String>();
        while (cursor.moveToNext()) {
            res.add(cursor.getString(NAME_INDEX));
        }
        cursor.close();

        return res;
    }

    private Cursor getListNamesCursor() {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] { KEY_ID, LIST_NAME };

        // Specify the where clause that will limit our results.
        String where = null;
        // Replace these with valid SQL statements as necessary.
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = modelDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelDBOpenHelper.LIST_TABLE, result_columns,
                where, whereArgs, groupBy, having, order);
        //
        return cursor;

    }

    public Cursor getTaskByListID(int id) {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] { KEY_ID, LIST_ID, STATE,
                STARTED_AT, FINISHED_AT, CONTENT };

        // Specify the where clause that will limit our results.
        String where = LIST_ID + "=" + id;

        // Replace these with valid SQL statements as necessary.
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = modelDBOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelDBOpenHelper.TASK_TABLE, result_columns,
                where, whereArgs, groupBy, having, order);
        //
        return cursor;

    }

    /**
     * Listing 8-2: Implementing an SQLite Open Helper
     */
    private static class ModelDBOpenHelper extends SQLiteOpenHelper {

        private final String TAG = getClass().getSimpleName();
        private static final boolean D = true;

        private static final String DATABASE_NAME = "octodo.db";
        private static final int DATABASE_VERSION = 2;

        private static final String TASK_TABLE = "task";
        private static final String LIST_TABLE = "list";

        // SQL Statement to create a new database.
        private static final String CREATE_TASK_TABLE = "create table "
                + TASK_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + CONTENT
                + " text not null, " + LIST_ID + " integer, " + STATE
                + " text not null, " + STARTED_AT
                + " timestamp default current_timestamp, " + FINISHED_AT
                + " timestamp" + ");";

        private static final String CREATE_LIST_TABLE = "create table "
                + LIST_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + LIST_NAME
                + " text not null, " + CREATED_AT
                + " timestamp default current_timestamp " + ");";

        public ModelDBOpenHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        private void addList(SQLiteDatabase db, String name) {
            ContentValues cv = new ContentValues();
            cv.put(LIST_NAME, name);
            db.insert(LIST_TABLE, null, cv);
        }

        // Called when no database exists in disk and the helper class needs
        // to create a new one.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TASK_TABLE);
            db.execSQL(CREATE_LIST_TABLE);

            // TODO: get the names of the lists from the res folder
            addList(db, "todo");
            addList(db, "done");
        }

        // Called when there is a database version mismatch meaning that
        // the version of the database on disk needs to be upgraded to
        // the current version.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data");

            // Upgrade the existing database to conform to the new
            // version. Multiple previous versions can be handled by
            // comparing oldVersion and newVersion values.

            // The simplest case is to drop the old table and create a new one.
            db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + LIST_TABLE);
            // Create a new one.
            onCreate(db);
        }
    }
}
