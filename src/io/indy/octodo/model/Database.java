package io.indy.octodo.model;

import io.indy.octodo.helper.DateFormatHelper;

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

    // list columns
    public static final String LIST_NAME = "name";
    public static final String CREATED_AT = "created_at";

    // task columns
    public static final String LIST_ID = "list_id";
    public static final String STATE = "state";
    public static final String STARTED_AT = "started_at";
    public static final String FINISHED_AT = "finished_at";
    public static final String CONTENT = "content";

    // Database open/upgrade helper
    private ModelHelper mModelHelper;

    public Database(Context context) {
        mModelHelper = new ModelHelper(context,
                ModelHelper.DATABASE_NAME,
                null,
                ModelHelper.DATABASE_VERSION);

        if (D)
            Log.d(TAG, "constructor");
    }

    // Called when you no longer need access to the database.
    public void closeDatabase() {
        mModelHelper.close();
    }

    // just uses the mContent field
    public void addTask(Task task) {
        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(LIST_ID, task.getListId());
        cv.put(CONTENT, task.getContent());
        cv.put(STATE, 0);

        Log.d(TAG, "inserting content: " + task.getContent());
        db.insert(ModelHelper.TASK_TABLE, null, cv);
    }

    public void updateTaskState(int id, int state) {
        Log.d(TAG, "Database:updateTaskState called");

        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATE, state);

        if(state == Task.STATE_STRUCK) {
            // task is effectively closed, so set the finished_at value
            String finishedDate = DateFormatHelper.today();
            cv.put(FINISHED_AT, finishedDate);
        }


        String where = KEY_ID + "=" + id;
        String whereArgs[] = null;

        db.update(ModelHelper.TASK_TABLE, cv, where, whereArgs);

    }

    public void updateTask(Task task) {
    }

    public void deleteTask(Task task) {
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(int taskListId) {
        Log.d(TAG, "Database:removeStruckTasks called");

        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATE, Task.STATE_CLOSED);

        String where = LIST_ID + "=" + taskListId + " AND " + STATE + "="
                + Task.STATE_STRUCK;
        String whereArgs[] = null;

        db.update(ModelHelper.TASK_TABLE, cv, where, whereArgs);
    }

    // return all the tasks associated with the list
    public List<Task> getTasks(int taskListId) {

        Cursor cursor = getTasksCursor(taskListId);

        int ID_INDEX = cursor.getColumnIndexOrThrow(KEY_ID);
        int STATE_INDEX = cursor.getColumnIndexOrThrow(STATE);
        int STARTED_AT_INDEX = cursor.getColumnIndexOrThrow(STARTED_AT);
        int FINISHED_AT_INDEX = cursor.getColumnIndexOrThrow(FINISHED_AT);
        int CONTENT_INDEX = cursor.getColumnIndexOrThrow(CONTENT);

        List<Task> res = new ArrayList<Task>();
        Task task;
        while (cursor.moveToNext()) {

            task = new Task.Builder().id(cursor.getInt(ID_INDEX))
                    .listId(taskListId)
                    .content(cursor.getString(CONTENT_INDEX))
                    .state(cursor.getInt(STATE_INDEX))
                    .startedAt(cursor.getString(STARTED_AT_INDEX))
                    .finishedAt(cursor.getString(FINISHED_AT_INDEX)).build();
            /*
             * task = new Task(cursor.getInt(ID_INDEX), taskListId,
             * cursor.getString(CONTENT_INDEX), cursor.getInt(STATE_INDEX));
             */
            res.add(task);
        }

        cursor.close();

        return res;
    }

    public List<TaskList> getTaskLists() {
        Cursor cursor = getTaskListsCursor();

        int ID_INDEX = cursor.getColumnIndexOrThrow(KEY_ID);
        int NAME_INDEX = cursor.getColumnIndexOrThrow(LIST_NAME);

        List<TaskList> res = new ArrayList<TaskList>();
        TaskList taskList;
        while (cursor.moveToNext()) {
            taskList = new TaskList(cursor.getInt(ID_INDEX),
                    cursor.getString(NAME_INDEX));
            res.add(taskList);
        }
        cursor.close();

        return res;
    }

    private Cursor getTaskListsCursor() {
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

        SQLiteDatabase db = mModelHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelHelper.LIST_TABLE,
                result_columns,
                where,
                whereArgs,
                groupBy,
                having,
                order);

        return cursor;
    }

    public Cursor getTasksCursor(int taskListId) {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] { KEY_ID, LIST_ID, STATE,
                STARTED_AT, FINISHED_AT, CONTENT };

        // Specify the where clause that will limit our results.
        String where = LIST_ID + "=? and " + STATE + "<?";

        // Replace these with valid SQL statements as necessary.
        String whereArgs[] = { Integer.toString(taskListId),
                Integer.toString(Task.STATE_CLOSED) };

        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = mModelHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelHelper.TASK_TABLE,
                result_columns,
                where,
                whereArgs,
                groupBy,
                having,
                order);
        //
        return cursor;
    }

    private static class ModelHelper extends SQLiteOpenHelper {

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

        public ModelHelper(Context context, String name, CursorFactory factory,
                int version) {
            super(context, name, factory, version);
        }

        private void createList(SQLiteDatabase db, String name) {
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
            createList(db, "todo");
            createList(db, "done");
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
