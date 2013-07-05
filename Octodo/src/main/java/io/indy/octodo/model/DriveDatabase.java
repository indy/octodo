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

import io.indy.octodo.async.UpdateTaskListsAsyncTask;

public class DriveDatabase {

    private static final String TAG = "DriveDatabase";
    private static final boolean D = true;

    private List<TaskList> mTaskLists;
    private List<TaskList> mHistoricTaskLists;

    private DriveManager mDriveManager;


    public void logTaskList(String taskListName) {
        TaskList taskList = getCurrentTaskList(taskListName);
        taskList.logTaskList();
    }

    public DriveDatabase(DriveManager driveManager) {
        mDriveManager = driveManager;
    }

    public DriveManager getDriveManager() {
        return mDriveManager;
    }

    // Called when you no longer need access to the database.
    public void closeDatabase() {
    }

    public void addList(String name) {
        if (name.equals("")) {
            Log.d(TAG, "attempting to add a tasklist with an empty name");
            return;
        }
    }

    public void deleteList(int id) {
        Log.d(TAG, "deleteList: " + id);
    }

    public void addTask(Task task, String taskListName) {

        TaskList taskList = getCurrentTaskList(taskListName);
        if(D) {
            Log.d(TAG, "addTask called! on tasklist: " + taskList);
        }

        taskList.add(task);
        saveCurrentTaskLists();
    }

    public void updateTaskContent(Task task, String content) {
        if (D) {
            Log.d(TAG, "updateTaskContent old: " + task.getContent() + " new: " + content);
        }

        task.setContent(content);
        saveCurrentTaskLists();
    }

    public void updateTaskState(Task task, int state) {
        if (D) {
            Log.d(TAG, "updateTaskState content:" + task.getContent() + " state: " + state);
        }

        task.setState(state);
        saveCurrentTaskLists();
    }

    // re-assign a task to a different tasklist
    public void moveTask(Task task, String source, String destination) {
        if (D) {
            Log.d(TAG, "moveTask " + task.getContent() + " to: " + destination);
        }

        TaskList sourceTaskList = getCurrentTaskList(source);
        TaskList destinationTaskList = getCurrentTaskList(destination);

        sourceTaskList.remove(task);
        destinationTaskList.add(task);

        saveCurrentTaskLists();
    }


    public void updateTask(Task task) {
    }

    // DELETE the specified task without adding it to the 'completed' tasklists
    public void deleteTask(Task task, String taskListName) {
        if (D) {
            Log.d(TAG, "deleteTask: getContent = " + task.getContent());
        }
        TaskList taskList = getCurrentTaskList(taskListName);
        taskList.remove(task);
        saveCurrentTaskLists();
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(String taskListName) {
        if (D) {
            Log.d(TAG, "removeStruckTasks: " + taskListName);
        }

        TaskList taskList = getCurrentTaskList(taskListName);

        // get (or make) tasklist in completed tasklists
        TaskList historicTaskList = getHistoricTaskList(taskListName);

        // iterate through tasklist and add into historic
        for(Task t: taskList.getTasks()) {
            if(t.getState() == Task.STATE_STRUCK) {
                t.setState(Task.STATE_CLOSED); // no real need for this in GDrive backend
                historicTaskList.add(t);
            }
        }

        // remove closed tasks from taskList
        taskList.getTasks().removeAll(historicTaskList.getTasks());

        // save all
        saveCurrentTaskLists();
        saveHistoricTaskLists();
    }

    private TaskList getHistoricTaskList(String name) {
        // get the TaskList called name from mHistoricTaskLists
        // if it doesn't exist, create it
        TaskList taskList = getTaskList(mHistoricTaskLists, name);
        if(taskList == null) {
            taskList = new TaskList(0, name);
            mHistoricTaskLists.add(taskList);
        }
        return taskList;
    }

    private TaskList getTaskList(List<TaskList> taskLists, String name) {

        for(TaskList tasklist: taskLists) {
            if(tasklist.getName().equals(name)) {
                return tasklist;
            }
        }
        return null;
    }

    // return all the tasks associated with the list
    public TaskList getCurrentTaskList(String name) {
        return getTaskList(mTaskLists, name);
    }

    public TaskList getCurrentTaskList(int index) {
        if(mTaskLists == null) {
            Log.d(TAG, "mTaskLists is empty");
            return null;
        } else if(mTaskLists.size() >= index){
            Log.d(TAG, "getCurrentTaskList index is too large");
            return null;
        }
        return mTaskLists.get(index);
    }

    public List<TaskList> getCurrentTaskLists() {
        return mTaskLists;
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> res = new ArrayList<TaskList>();
        return res;
    }

    private static final String HEADER = "header";
    private static final String BODY = "body";

    private static List<TaskList> buildDefaultEmptyTaskLists() {
        List<TaskList> tasklists = new ArrayList<TaskList>();
        tasklists.add(new TaskList(0, "today"));
        tasklists.add(new TaskList(0, "this week"));

        return tasklists;
    }

    public static List<TaskList> fromJSON(JSONObject json) {

        List<TaskList> tasklists = new ArrayList<TaskList>();

        try {

            // json has form of:
            // { header: {}, body: array of tasklists}

            if(json.isNull(BODY)) {
                // empty body so default to empty today and thisweek tasklists
                Log.d(TAG, "fromJSON: empty body so defaulting to empty today and thisweek tasklists");
                return buildDefaultEmptyTaskLists();
            }
            // ignore header for now, in future this will have version info

            // parse the body which should be a list of tasklists
            //
            JSONArray body = json.getJSONArray(BODY);
            TaskList tasklist;
            for(int i=0; i<body.length(); i++) {
                tasklist = TaskList.fromJson(body.getJSONObject(i));
                tasklists.add(tasklist);
            }

        } catch (JSONException e) {
            Log.d(TAG, "fromJSON JSONException: " + e);
            Log.d(TAG, "defaulting to empty today and thisweek tasklists");
            tasklists = buildDefaultEmptyTaskLists();
        }

        return tasklists;
    }

    private JSONObject toJson(List<TaskList> tasklists) {
        JSONObject res = new JSONObject();

        try {
            res.put(HEADER, JSONObject.NULL);

            JSONArray array = new JSONArray();
            for(TaskList t : tasklists) {
                JSONObject obj = t.toJson();
                array.put(obj);
            }
            res.put(BODY, array);

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return res;
    }

    private void saveCurrentTaskLists() {
        /*
        JSONObject current = currentToJson();
        try {
            Log.d(TAG, current.toString(2));
        } catch(JSONException e) {
            Log.e(TAG, e.toString());
        }
        */

        // launch an asyncTask that updates the current json file
        JSONObject json = currentToJson();
        new UpdateTaskListsAsyncTask(mDriveManager, DriveManager.CURRENT_JSON, json).execute();
    }

    private void saveHistoricTaskLists() {
        JSONObject json = historicToJson();
        new UpdateTaskListsAsyncTask(mDriveManager, DriveManager.HISTORIC_JSON, json).execute();
    }

    public JSONObject currentToJson() {
        return toJson(mTaskLists);
    }

    public JSONObject historicToJson() {
        return toJson(mHistoricTaskLists);
    }

    public void setCurrentTaskLists(List<TaskList> tasklists) {
        mTaskLists = tasklists;
    }

    public void setHistoricTaskLists(List<TaskList> tasklists) {
        mHistoricTaskLists = tasklists;
    }

}
