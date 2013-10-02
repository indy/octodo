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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.DriveBaseActivity;
import io.indy.octodo.OctodoApplication;
import io.indy.octodo.async.CurrentTaskListsAsyncTask;
import io.indy.octodo.async.HistoricTaskListsAsyncTask;
import io.indy.octodo.event.LoadedTaskListsEvent;

public class OctodoModel {

    static private final boolean D = true;
    static private final String TAG = OctodoModel.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    public static final int NOT_LOADED = 0;
    public static final int LOADED_FROM_FILE = 1;   // Local disk
    public static final int LOADED_FROM_DRIVE = 2;  // Google Drive


    private DriveBaseActivity mContext;

    // ram storage
    private OctodoApplication mApplication;
    // server side storage
    private DriveStorage mDriveStorage;


    public OctodoModel(DriveBaseActivity activity) {
        mContext = activity;
        mApplication = (OctodoApplication) activity.getApplication();
        ifd("mApplication is " + System.identityHashCode(mApplication));
    }

    public void onDriveDatabaseInitialised() {
        mDriveStorage = mContext.getDriveStorage();
    }

    public void initFromFile() {
        ifd("initFromFile");

        // load data from atomic file
        AtomicStorage atomicStorage = new AtomicStorage(mContext);
        TaskListsPack taskListsPack;

        JSONObject json = atomicStorage.getJSON(AtomicStorage.CURRENT_FILENAME);
        if (json == null) {
            taskListsPack = TaskListsPack.buildEmptyTaskListsPack();
        } else {
            taskListsPack = TaskListsPack.fromJSON(json);
        }
        onLoadedCurrentTaskLists(taskListsPack, OctodoModel.LOADED_FROM_FILE);

        json = atomicStorage.getJSON(AtomicStorage.HISTORIC_FILENAME);
        if (json == null) {
            taskListsPack = TaskListsPack.buildEmptyTaskListsPack();
        } else {
            taskListsPack = TaskListsPack.fromJSON(json);
        }
        onLoadedHistoricTaskLists(taskListsPack, OctodoModel.LOADED_FROM_FILE);
    }

    public DriveStorage getDriveStorage() {
        return mDriveStorage;
    }

    public void onLoadedCurrentTaskLists(TaskListsPack taskListsPack, int loadSource) {

        boolean overWritten = mApplication.setCurrentTaskLists(taskListsPack, loadSource);

        // if data from Google Drive is more recent, copy it into AtomicStorage
        if (loadSource == OctodoModel.LOADED_FROM_DRIVE) {
            maybeRefreshAtomicWithDrive(taskListsPack, AtomicStorage.CURRENT_FILENAME);
        }

        // fire event to update the UI
        LoadedTaskListsEvent event = new LoadedTaskListsEvent(overWritten, loadSource);
        EventBus.getDefault().post(event);
    }

    public void onLoadedHistoricTaskLists(TaskListsPack taskListsPack, int loadSource) {
        ifd("onLoadedHistoricTaskLists");

        mApplication.setHistoricTaskLists(taskListsPack, loadSource);

        // if data from Google Drive is more recent, copy it into AtomicStorage
        if (loadSource == OctodoModel.LOADED_FROM_DRIVE) {
            maybeRefreshAtomicWithDrive(taskListsPack, AtomicStorage.HISTORIC_FILENAME);
        }
    }

    private void maybeRefreshAtomicWithDrive(TaskListsPack taskListsPack, String atomicFilename) {
        AtomicStorage atomicStorage = new AtomicStorage(mContext);
        JSONObject json = atomicStorage.getJSON(atomicFilename);
        if (json != null) {
            Date localDate = TaskListsPack.parseJSONHeader(json);
            if (localDate.before(taskListsPack.getModifiedDate())) {
                // date from Drive is more recent than on local storage
                // so overwrite local storage
                atomicStorage.saveJSON(atomicFilename, taskListsPack.toJson());
            }
        }
    }

    public List<TaskList> getCurrentTaskLists() {
        return mApplication.getCurrentTaskLists();
    }

    public List<TaskList> getHistoricTaskLists() {
        return mApplication.getHistoricTaskLists();
    }

    public boolean hasLoadedTaskListFrom(int loadSource) {
        return mApplication.hasLoadedTaskListFrom(loadSource);
    }

    public void addList(String name) {
        if (name.equals("")) {
            ifd("attempting to add a tasklist with an empty name");
            return;
        }

        TaskList existing = getCurrentTaskList(name);
        if (existing != null) {
            ifd("addList: a list with the name " + name + " already exists");
            return;
        }

        TaskList taskList = new TaskList(name);
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();
        taskLists.add(taskList);
        saveCurrentTaskListsToDrive();
    }

    private void saveCurrentTaskListsToDrive() {
        Date today = new Date();
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();
        TaskListsPack taskListsPack = new TaskListsPack(today, taskLists);

        JSONObject json = taskListsPack.toJson();

        ifd("saveCurrentTaskListsToDrive: " + json.toString());

        AtomicStorage atomicStorage = new AtomicStorage(mContext);
        atomicStorage.saveJSON(AtomicStorage.CURRENT_FILENAME, json);

        mDriveStorage.saveCurrentTaskLists(json);
    }

    private void saveHistoricTaskListsToDrive() {
        Date today = new Date();
        List<TaskList> taskLists = mApplication.getHistoricTaskLists();
        TaskListsPack taskListsPack = new TaskListsPack(today, taskLists);

        JSONObject json = taskListsPack.toJson();

        ifd("saveHistoricTaskListsToDrive: " + json.toString());

        AtomicStorage atomicStorage = new AtomicStorage(mContext);
        atomicStorage.saveJSON(AtomicStorage.HISTORIC_FILENAME, json);

        mDriveStorage.saveHistoricTaskLists(json);
    }

    public boolean deleteList(String name) {
        ifd("deleteList " + name);
        TaskList taskList = this.getCurrentTaskList(name);
        if (taskList == null) {
            return false;
        }

        List<TaskList> taskLists = mApplication.getCurrentTaskLists();
        taskLists.remove(taskList);
        saveCurrentTaskListsToDrive();
        return true;
    }

    public void addTask(Task task, String taskListName) {

        TaskList taskList = getCurrentTaskList(taskListName);
        ifd("addTask called! on tasklist: " + taskList);

        taskList.add(task);
        saveCurrentTaskListsToDrive();
    }

    public void updateTaskContent(Task task, String content) {
        ifd("updateTaskContent old: " + task.getContent() + " new: " + content);

        task.setContent(content);
        saveCurrentTaskListsToDrive();
    }

    public void updateTaskState(Task task, int state) {
        ifd("updateTaskState content:" + task.getContent() + " state: " + state);

        task.setState(state);
        saveCurrentTaskListsToDrive();
    }

    public void updateTaskParent(Task task, String newTaskList) {
        TaskList sourceTaskList = getCurrentTaskList(task.getParentName());
        TaskList destinationTaskList = getCurrentTaskList(newTaskList);

        if (sourceTaskList != destinationTaskList) {
            sourceTaskList.remove(task);
            destinationTaskList.add(task);
        }

        saveCurrentTaskListsToDrive();
    }

    // DELETE the specified task without adding it to the 'completed' tasklists
    public void deleteTask(Task task) {
        ifd("deleteTask: getContent = " + task.getContent());

        TaskList taskList = getCurrentTaskList(task.getParentName());
        taskList.remove(task);
        saveCurrentTaskListsToDrive();
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(String taskListName) {
        ifd("removeStruckTasks: " + taskListName);

        TaskList taskList = getCurrentTaskList(taskListName);

        // get (or make) tasklist in completed tasklists
        TaskList historicTaskList = getHistoricTaskList(taskListName);

        // iterate through tasklist and add into historic
        for (Task t : taskList.getTasks()) {
            if (t.getState() == Task.STATE_STRUCK) {
                t.setState(Task.STATE_CLOSED); // no real need for this in GDrive backend
                historicTaskList.add(t);
            }
        }

        // remove closed tasks from taskList
        taskList.getTasks().removeAll(historicTaskList.getTasks());

        // save all
        saveCurrentTaskListsToDrive();
        saveHistoricTaskListsToDrive();
    }

    private TaskList getHistoricTaskList(String name) {
        // get the TaskList called name from mHistoricTaskLists
        // if it doesn't exist, create it
        List<TaskList> historicTaskLists = mApplication.getHistoricTaskLists();

        TaskList taskList = getTaskList(historicTaskLists, name);
        if (taskList == null) {
            taskList = new TaskList(name);
            historicTaskLists.add(taskList);
        }
        return taskList;
    }

    private TaskList getTaskList(List<TaskList> taskLists, String name) {
        if (taskLists == null) {
            ifd("getTaskList given null taskLists when searching for " + name);
            return null;
        }

        for (TaskList tasklist : taskLists) {
            if (tasklist.getName().equals(name)) {
                return tasklist;
            }
        }

        ifd("unable to find taskList called: " + name);
        return null;
    }

    // return all the tasks associated with the list
    public TaskList getCurrentTaskList(String name) {
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();
        return getTaskList(taskLists, name);
    }

    public TaskList getCurrentTaskList(int index) {
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();

        if (taskLists == null) {
            ifd("mTaskLists is empty");
            return null;
        } else if (index >= taskLists.size()) {
            ifd("getCurrentTaskList index is too large");
            return null;
        }
        return taskLists.get(index);
    }

    public void deleteSelectedTaskLists() {
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();
        List<TaskList> removable = new ArrayList<TaskList>();

        for (TaskList taskList : taskLists) {
            if (taskList.isSelected()) {
                removable.add(taskList);
            }
        }

        for (TaskList taskList : removable) {
            taskLists.remove(taskList);
        }

        saveCurrentTaskListsToDrive();
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> taskLists = mApplication.getCurrentTaskLists();

        List<TaskList> res = new ArrayList<TaskList>();
        for (TaskList taskList : taskLists) {
            res.add(taskList);
        }
        return res;
    }

    public void asyncLoadCurrentTaskLists() {
        new CurrentTaskListsAsyncTask(this).execute();
    }

    public void asyncLoadHistoricTaskLists() {
        new HistoricTaskListsAsyncTask(this).execute();
    }

}
