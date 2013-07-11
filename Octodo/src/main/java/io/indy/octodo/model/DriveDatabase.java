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

import java.util.ArrayList;
import java.util.List;

import io.indy.octodo.async.HistoricTaskListsAsyncTask;
import io.indy.octodo.async.TaskListsAsyncTask;

public class DriveDatabase {

    private static final String TAG = "DriveDatabase";
    private static final boolean D = true;

    private DriveManager mDriveManager;

    public DriveDatabase(DriveManager driveManager) {
        mDriveManager = driveManager;
    }

    public void addList(String name) {
        if (name.equals("")) {
            Log.d(TAG, "attempting to add a tasklist with an empty name");
            return;
        }

        TaskList existing = getCurrentTaskList(name);
        if(existing != null) {
            Log.d(TAG, "addList: a list with the name " + name + " already exists");
            return;
        }

        TaskList taskList = new TaskList(0, name);
        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();
        taskLists.add(taskList);
        mDriveManager.saveCurrentTaskLists();
    }

    public boolean deleteList(String name) {
        Log.d(TAG, "deleteList " + name);
        TaskList taskList = this.getCurrentTaskList(name);
        if(taskList == null) {
            return false;
        }

        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();
        taskLists.remove(taskList);
        mDriveManager.saveCurrentTaskLists();
        return true;
    }

    public void addTask(Task task, String taskListName) {

        TaskList taskList = getCurrentTaskList(taskListName);
        if(D) {
            Log.d(TAG, "addTask called! on tasklist: " + taskList);
        }

        taskList.add(task);
        task.setParentName(taskListName);
        mDriveManager.saveCurrentTaskLists();
    }

    public void updateTaskContent(Task task, String content) {
        if (D) {
            Log.d(TAG, "updateTaskContent old: " + task.getContent() + " new: " + content);
        }

        task.setContent(content);
        mDriveManager.saveCurrentTaskLists();
    }

    public void updateTaskState(Task task, int state) {
        if (D) {
            Log.d(TAG, "updateTaskState content:" + task.getContent() + " state: " + state);
        }

        task.setState(state);
        mDriveManager.saveCurrentTaskLists();
    }

    // re-assign a task to a different tasklist
    public void moveTask(Task task, String destination) {
        if (D) {
            Log.d(TAG, "moveTask " + task.getContent() + " to: " + destination);
        }

        TaskList sourceTaskList = getCurrentTaskList(task.getParentName());
        TaskList destinationTaskList = getCurrentTaskList(destination);

        sourceTaskList.remove(task);
        destinationTaskList.add(task);

        task.setParentName(destination);

        mDriveManager.saveCurrentTaskLists();
    }


    public void updateTask(Task task) {
    }

    // DELETE the specified task without adding it to the 'completed' tasklists
    public void deleteTask(Task task) {
        if (D) {
            Log.d(TAG, "deleteTask: getContent = " + task.getContent());
        }
        TaskList taskList = getCurrentTaskList(task.getParentName());
        taskList.remove(task);
        mDriveManager.saveCurrentTaskLists();
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
        mDriveManager.saveCurrentTaskLists();
        mDriveManager.saveHistoricTaskLists();
    }

    private TaskList getHistoricTaskList(String name) {
        // get the TaskList called name from mHistoricTaskLists
        // if it doesn't exist, create it

        List<TaskList> historicTaskLists = mDriveManager.getHistoricTaskLists();

        TaskList taskList = getTaskList(historicTaskLists, name);
        if(taskList == null) {
            taskList = new TaskList(0, name);
            historicTaskLists.add(taskList);
        }
        return taskList;
    }

    private TaskList getTaskList(List<TaskList> taskLists, String name) {

        if(taskLists == null) {
            Log.d(TAG, "getTaskList given null taskLists when searching for " + name);
            return null;
        }

        for(TaskList tasklist: taskLists) {
            if(tasklist.getName().equals(name)) {
                return tasklist;
            }
        }

        Log.d(TAG, "unable to find taskList called: " + name);
        return null;
    }

    // return all the tasks associated with the list
    public TaskList getCurrentTaskList(String name) {
        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();
        return getTaskList(taskLists, name);
    }

    public TaskList getCurrentTaskList(int index) {
        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();

        if(taskLists == null) {
            Log.d(TAG, "mTaskLists is empty");
            return null;
        } else if(index >= taskLists.size()){
            Log.d(TAG, "getCurrentTaskList index is too large");
            return null;
        }
        return taskLists.get(index);
    }

    public List<TaskList> getCurrentTaskLists() {
        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();
        return taskLists;
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> taskLists = mDriveManager.getCurrentTaskLists();

        List<TaskList> res = new ArrayList<TaskList>();
        for(TaskList taskList: taskLists) {
            if(taskList.isDeleteable()) {
                res.add(taskList);
            }
        }
        return res;
    }

    public boolean hasLoadedTaskLists() {
        return mDriveManager.hasLoadedTaskLists();
    }

    public void asyncLoadCurrentTaskLists() {
        new TaskListsAsyncTask(mDriveManager).execute();
    }

    public void asyncLoadHistoricTaskLists() {
        new HistoricTaskListsAsyncTask(mDriveManager).execute();
    }

}
