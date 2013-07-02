
package io.indy.octodo.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.indy.octodo.helper.DateFormatHelper;

public class SQLDatabase {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    // The index (key) column name for use in where clauses.
    public static final String KEY_ID = "id";

    // The name and column index of each column in your database.
    // These should be descriptive.

    // list columns
    //
    public static final String LIST_NAME = "name";

    public static final String CREATED_AT = "created_at";

    public static final String DELETED_AT = "deleted_at";

    // do the tasks in the list have an expected completion timespan?
    public static final String HAS_TASK_LIFETIME = "has_task_lifetime";

    // the number of hours a task is expected to take
    public static final String TASK_LIFETIME = "task_lifetime";

    public static final String IS_DELETEABLE = "is_deleteable";

    // task columns
    //
    public static final String LIST_ID = "list_id";

    public static final String STARTED_AT = "started_at";

    public static final String FINISHED_AT = "finished_at";

    public static final String CONTENT = "content";

    // shared columns
    public static final String STATE = "state";

    // SQLDatabase open/upgrade helper
    private ModelHelper mModelHelper;

    public SQLDatabase(Context context) {
        mModelHelper = new ModelHelper(context, ModelHelper.DATABASE_NAME, null,
                ModelHelper.DATABASE_VERSION);

        if (D)
            Log.d(TAG, "constructor");
    }

    // Called when you no longer need access to the database.
    public void closeDatabase() {
        mModelHelper.close();
    }

    // TODO: pass in a proper 'task list' object?
    // TODO: return values to indicate success?
    public void addList(String name) {
        if (name.equals("")) {
            Log.d(TAG, "attempting to add a tasklist with an empty name");
            return;
        }

        SQLiteDatabase db = mModelHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LIST_NAME, name);
        cv.put(STATE, TaskList.STATE_ACTIVE);
        // user created lists don't have task lifetimes (yet)
        cv.put(HAS_TASK_LIFETIME, 0);
        cv.put(TASK_LIFETIME, 0);

        db.insert(ModelHelper.LIST_TABLE, null, cv);
    }

    public void deleteList(int id) {

        Log.d(TAG, "deleteList: " + id);

        SQLiteDatabase db = mModelHelper.getWritableDatabase();
        String finishedDate = DateFormatHelper.today();

        ContentValues cv = new ContentValues();

        cv.put(STATE, TaskList.STATE_INACTIVE);
        cv.put(DELETED_AT, finishedDate);

        String where = KEY_ID + "=" + id;
        String whereArgs[] = null;

        db.update(ModelHelper.LIST_TABLE, cv, where, whereArgs);
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

    public void updateTaskContent(int taskId, String content) {
        if (D) {
            Log.d(TAG, "updateTaskContent id: " + taskId + " content: " + content);
        }

        ContentValues cv = new ContentValues();
        cv.put(CONTENT, content);

        updateTaskTable(taskId, cv);

    }

    public void updateTaskState(int taskId, int state) {
        if (D) {
            Log.d(TAG, "updateTaskState id:" + taskId + " state: " + state);
        }

        ContentValues cv = new ContentValues();

        cv.put(STATE, state);
        if (state == Task.STATE_STRUCK) {
            // task is effectively closed, so set the finished_at value
            String finishedDate = DateFormatHelper.today();
            cv.put(FINISHED_AT, finishedDate);
        }

        updateTaskTable(taskId, cv);
    }

    // re-assign a task to a different tasklist
    public void updateTaskParentList(int taskId, int taskListId) {
        if (D) {
            Log.d(TAG, "updateTaskParentList taskId: " + taskId + " taskListId: " + taskListId);
        }

        ContentValues cv = new ContentValues();
        cv.put(LIST_ID, taskListId);

        updateTaskTable(taskId, cv);
    }

    private void updateTaskTable(int taskId, ContentValues cv) {
        if (D) {
            Log.d(TAG, "updateTaskTable: taskId=" + taskId);
        }

        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        String where = KEY_ID + "=?";
        String whereArgs[] = {
            Integer.toString(taskId)
        };

        db.update(ModelHelper.TASK_TABLE, cv, where, whereArgs);
    }

    public void updateTask(Task task) {
    }

    // DELETE the specified task
    public void deleteTask(int taskId) {
        if (D) {
            Log.d(TAG, "deleteTask: taskId=" + taskId);
        }

        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        String where = KEY_ID + "=?";
        String whereArgs[] = {
            Integer.toString(taskId)
        };

        db.delete(ModelHelper.TASK_TABLE, where, whereArgs);
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(int taskListId) {
        if (D) {
            Log.d(TAG, "removeStruckTasks: taskListId=" + taskListId);
        }

        SQLiteDatabase db = mModelHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(STATE, Task.STATE_CLOSED);

        String where = LIST_ID + "=" + taskListId + " AND " + STATE + "=" + Task.STATE_STRUCK;
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

            task = new Task.Builder().id(cursor.getInt(ID_INDEX))//.listId(taskListId)
                    .content(cursor.getString(CONTENT_INDEX)).state(cursor.getInt(STATE_INDEX))
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
        Cursor cursor = getActiveTaskListsCursor();

        int ID_INDEX = cursor.getColumnIndexOrThrow(KEY_ID);
        int NAME_INDEX = cursor.getColumnIndexOrThrow(LIST_NAME);

        List<TaskList> res = new ArrayList<TaskList>();
        TaskList taskList;
        while (cursor.moveToNext()) {
            taskList = new TaskList(cursor.getInt(ID_INDEX), cursor.getString(NAME_INDEX));
            res.add(taskList);
        }
        cursor.close();

        return res;
    }

    public List<TaskList> getDeleteableTaskLists() {
        Cursor cursor = getDeleteableTaskListsCursor();

        int ID_INDEX = cursor.getColumnIndexOrThrow(KEY_ID);
        int NAME_INDEX = cursor.getColumnIndexOrThrow(LIST_NAME);

        List<TaskList> res = new ArrayList<TaskList>();
        TaskList taskList;
        while (cursor.moveToNext()) {
            taskList = new TaskList(cursor.getInt(ID_INDEX), cursor.getString(NAME_INDEX));
            res.add(taskList);
        }
        cursor.close();

        return res;
    }

    // returns cursor to all active taskLists
    //
    private Cursor getDeleteableTaskListsCursor() {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] {
                KEY_ID, LIST_NAME
        };

        String where = STATE + "=?" + " and " + IS_DELETEABLE + "=?";
        // active and deletable task lists
        String whereArgs[] = {
                Integer.toString(TaskList.STATE_ACTIVE), Integer.toString(1)
        };

        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = mModelHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelHelper.LIST_TABLE, result_columns, where, whereArgs, groupBy,
                having, order);

        return cursor;
    }

    // returns cursor to all active taskLists
    //
    private Cursor getActiveTaskListsCursor() {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] {
                KEY_ID, LIST_NAME
        };

        String where = STATE + "=?";
        String whereArgs[] = {
            Integer.toString(TaskList.STATE_ACTIVE)
        };

        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = mModelHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelHelper.LIST_TABLE, result_columns, where, whereArgs, groupBy,
                having, order);

        return cursor;
    }

    public Cursor getTasksCursor(int taskListId) {
        // Specify the result column projection. Return the minimum set
        // of columns required to satisfy your requirements.
        String[] result_columns = new String[] {
                KEY_ID, LIST_ID, STATE, STARTED_AT, FINISHED_AT, CONTENT
        };

        String where = LIST_ID + "=? and " + STATE + "<?";
        String whereArgs[] = {
                Integer.toString(taskListId), Integer.toString(Task.STATE_CLOSED)
        };

        String groupBy = null;
        String having = null;
        String order = null;

        SQLiteDatabase db = mModelHelper.getReadableDatabase();
        Cursor cursor = db.query(ModelHelper.TASK_TABLE, result_columns, where, whereArgs, groupBy,
                having, order);

        return cursor;
    }

    private static class ModelHelper extends SQLiteOpenHelper {

        private final String TAG = getClass().getSimpleName();

        private static final boolean D = true;

        private static final String DATABASE_NAME = "octodo.db";

        private static final int DATABASE_VERSION = 5;

        private static final String TASK_TABLE = "task";

        private static final String LIST_TABLE = "list";

        public ModelHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        private void createList(SQLiteDatabase db, String name, int isDeleteable) {
            ContentValues cv = new ContentValues();
            cv.put(LIST_NAME, name);
            cv.put(STATE, TaskList.STATE_ACTIVE);
            cv.put(IS_DELETEABLE, isDeleteable);

            cv.put(HAS_TASK_LIFETIME, 0);

            db.insert(LIST_TABLE, null, cv);
        }

        private void createList(SQLiteDatabase db, String name, int isDeleteable, int lifetimeHours) {
            ContentValues cv = new ContentValues();
            cv.put(LIST_NAME, name);
            cv.put(STATE, TaskList.STATE_ACTIVE);
            cv.put(IS_DELETEABLE, isDeleteable);

            cv.put(HAS_TASK_LIFETIME, 1);
            cv.put(TASK_LIFETIME, lifetimeHours);

            db.insert(LIST_TABLE, null, cv);
        }

        // Called when no database exists in disk and the helper class needs
        // to create a new one.
        @Override
        public void onCreate(SQLiteDatabase db) {
            if (D) {
                Log.d(TAG, "onCreate");
            }

            String createTaskTable = new SQLTableStatement(TASK_TABLE)
                    .addInteger(KEY_ID, "primary key autoincrement").addText(CONTENT, "not null")
                    .addInteger(LIST_ID).addInteger(STATE)
                    .addTimestamp(STARTED_AT, "default current_timestamp")
                    .addTimestamp(FINISHED_AT).create();
            db.execSQL(createTaskTable);

            String createListTable = new SQLTableStatement(LIST_TABLE)
                    .addInteger(KEY_ID, "primary key autoincrement").addText(LIST_NAME, "not null")
                    .addInteger(STATE).addInteger(HAS_TASK_LIFETIME, "default 0")
                    .addInteger(TASK_LIFETIME, "default 0").addInteger(IS_DELETEABLE, "default 1")
                    .addTimestamp(CREATED_AT, "default current_timestamp").addTimestamp(DELETED_AT)
                    .create();
            db.execSQL(createListTable);

            // TODO: get the names of the lists from the res folder
            createList(db, "today", 0, 24);
            createList(db, "this week", 0, 24 * 7);
            createList(db, "later", 1);
        }

        // Called when there is a database version mismatch meaning that
        // the version of the database on disk needs to be upgraded to
        // the current version.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");

            // Upgrade the existing database to conform to the new
            // version. Multiple previous versions can be handled by
            // comparing oldVersion and newVersion values.

            // The simplest case is to drop the old table and create a new one.
            String dropTaskTable = new SQLTableStatement(TASK_TABLE).drop();
            db.execSQL(dropTaskTable);

            String dropListTable = new SQLTableStatement(LIST_TABLE).drop();
            db.execSQL(dropListTable);

            // Create a new one.
            onCreate(db);
        }
    }
}
