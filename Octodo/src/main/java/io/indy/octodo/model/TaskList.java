/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.indy.octodo.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskList {

    static private final boolean D = true;
    static private final String TAG = TaskList.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private final String mName;
    private List<Task> mTasks;

    // set by ManageListsAdapter
    private boolean mIsSelected;
    private boolean mIsDeleteable;

    public static final int STATE_ACTIVE = 0;

    public static final int STATE_INACTIVE = 1;

    private static final String NAME = "name";
    private static final String STATE = "state";
    private static final String HAS_TASK_LIFETIME = "has_task_lifetime";
    private static final String TASK_LIFETIME = "task_lifetime";
    private static final String IS_DELETEABLE = "is_deleteable";
    private static final String CREATED_AT = "created_at";
    private static final String DELETED_AT = "deleted_at";
    // name given to json array of tasks
    private static final String TASKS = "tasks";

    public boolean hasStruckTasks() {
        for (Task task : mTasks) {
            if (task.getState() == Task.STATE_STRUCK) {
                return true;
            }
        }
        return false;
    }

    public void logTaskList() {
        ifd("logTaskList: " + mName);
        for (Task t : mTasks) {
            int id = System.identityHashCode(t);
            ifd("task: [" + id + "] " + t.getContent());
        }
    }

    // use a builder similar to the Task one
    public TaskList(String name) {
        mName = name;
        mTasks = new ArrayList<Task>();

        mIsSelected = false;
        mIsDeleteable = true;
    }

    public void setDeleteable(boolean isDeleteable) {
        mIsDeleteable = isDeleteable;
    }

    public boolean isDeleteable() {
        return mIsDeleteable;
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public TaskList add(Task task) {
        mTasks.add(task);
        task.setParentName(mName);
        return this;
    }

    public TaskList remove(Task task) {
        boolean isRemoved = mTasks.remove(task);

        if (!isRemoved) {
            Log.e(TAG, "unable to remove a task from taskList");
            Log.e(TAG, "TaskList: ");
            logTaskList();
            Log.e(TAG, "Task: ");
            task.debug();
        }

        return this;
    }

    public static TaskList fromJson(JSONObject jsonObject) {

        try {
            String name = jsonObject.getString(NAME);

            TaskList taskList = new TaskList(name);

            JSONArray jsonTasks = jsonObject.getJSONArray(TASKS);
            for (int i = 0; i < jsonTasks.length(); i++) {
                JSONObject jsonTask = jsonTasks.getJSONObject(i);
                Task task = Task.fromJson(jsonTask);
                taskList.add(task);
            }

            return taskList;

        } catch (JSONException e) {
            ifd("JSONException: " + e);
        }

        return null;
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();

        try {
            res.put(NAME, mName);
            res.put(STATE, 0);
            res.put(HAS_TASK_LIFETIME, 0);
            res.put(TASK_LIFETIME, 0);
            res.put(IS_DELETEABLE, mIsDeleteable);
            res.put(CREATED_AT, 0);
            res.put(DELETED_AT, 0);

            // the list of tasks

            JSONArray array = new JSONArray();
            for (Task t : mTasks) {
                JSONObject obj = t.toJson();
                array.put(obj);
            }
            res.put(TASKS, array);

        } catch (JSONException e) {
            ifd("JSONException: " + e);
        }

        return res;
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
        return mName + " " + System.identityHashCode(this);

    }

}
