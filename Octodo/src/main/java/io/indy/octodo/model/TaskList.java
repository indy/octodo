
package io.indy.octodo.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskList {

    private static final String TAG = "TaskList";
    private static final boolean D = true;

    private final int mId;
    private final String mName;
    private List<Task> mTasks;

    // set by ManageListsAdapter
    private boolean mIsSelected;
    private boolean mIsDeleteable;

    public static final int STATE_ACTIVE = 0;

    public static final int STATE_INACTIVE = 1;

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String STATE = "state";
    private static final String HAS_TASK_LIFETIME = "has_task_lifetime";
    private static final String TASK_LIFETIME = "task_lifetime";
    private static final String IS_DELETEABLE = "is_deleteable";
    private static final String CREATED_AT = "created_at";
    private static final String DELETED_AT = "deleted_at";
    // name given to json array of tasks
    private static final String TASKS = "tasks";

    // use a builder similar to the Task one
    public TaskList(int id, String name) {
        mId = id;
        mName = name;
        mTasks = new ArrayList<Task>();

        mIsSelected = false;
        mIsDeleteable = true;
    }

    public TaskList add(Task task) {
        mTasks.add(task);
        return this;
    }

    public static TaskList fromJson(JSONObject jsonObject) {

        try {
            int id = jsonObject.getInt(ID);
            String name = jsonObject.getString(NAME);

            TaskList taskList = new TaskList(id, name);

            JSONArray jsonTasks = jsonObject.getJSONArray(TASKS);
            for(int i=0;i<jsonTasks.length();i++) {
                JSONObject jsonTask = jsonTasks.getJSONObject(i);
                Task task = Task.fromJson(jsonTask);
                taskList.add(task);
            }

            return taskList;

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return null;
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();

        try {
            res.put(ID, mId);
            res.put(NAME, mName);
            res.put(STATE, 0);
            res.put(HAS_TASK_LIFETIME, 0);
            res.put(TASK_LIFETIME, 0);
            res.put(IS_DELETEABLE, mIsDeleteable);
            res.put(CREATED_AT, 0);
            res.put(DELETED_AT, 0);

            // the list of tasks

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return res;
    }


    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    @Override
    public String toString() {
        return mName;
    }

}
