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

public class DriveModel {

    static private final boolean D = true;
    static private final String TAG = DriveModel.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private DriveDatabase mDriveDatabase;

    public DriveModel(DriveDatabase driveDatabase) {
        mDriveDatabase = driveDatabase;
    }

    public void addList(String name) {
        if (name.equals("")) {
            ifd("attempting to add a tasklist with an empty name");
            return;
        }

        TaskList existing = getCurrentTaskList(name);
        if(existing != null) {
            ifd("addList: a list with the name " + name + " already exists");
            return;
        }

        TaskList taskList = new TaskList(0, name);
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();
        taskLists.add(taskList);
        mDriveDatabase.saveCurrentTaskLists();
    }

    public boolean deleteList(String name) {
        ifd("deleteList " + name);
        TaskList taskList = this.getCurrentTaskList(name);
        if(taskList == null) {
            return false;
        }

        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();
        taskLists.remove(taskList);
        mDriveDatabase.saveCurrentTaskLists();
        return true;
    }

    public void addTask(Task task, String taskListName) {

        TaskList taskList = getCurrentTaskList(taskListName);
        ifd("addTask called! on tasklist: " + taskList);

        taskList.add(task);
        task.setParentName(taskListName);
        mDriveDatabase.saveCurrentTaskLists();
    }

    public void updateTaskContent(Task task, String content) {
        ifd("updateTaskContent old: " + task.getContent() + " new: " + content);

        task.setContent(content);
        mDriveDatabase.saveCurrentTaskLists();
    }

    public void updateTaskState(Task task, int state) {
        ifd("updateTaskState content:" + task.getContent() + " state: " + state);

        task.setState(state);
        mDriveDatabase.saveCurrentTaskLists();
    }

    // re-assign a task to a different tasklist
    public void moveTask(Task task, String destination) {
        ifd("moveTask " + task.getContent() + " to: " + destination);

        TaskList sourceTaskList = getCurrentTaskList(task.getParentName());
        TaskList destinationTaskList = getCurrentTaskList(destination);

        sourceTaskList.remove(task);
        destinationTaskList.add(task);

        task.setParentName(destination);

        mDriveDatabase.saveCurrentTaskLists();
    }

    public void editedTask(Task task, String newContent, String newTaskList) {

        if(!newContent.equals(task.getContent())) {
            task.setContent(newContent);
        }

        TaskList sourceTaskList = getCurrentTaskList(task.getParentName());
        TaskList destinationTaskList = getCurrentTaskList(newTaskList);

        if(sourceTaskList != destinationTaskList) {
            sourceTaskList.remove(task);
            destinationTaskList.add(task);
            task.setParentName(newTaskList);
        }

        mDriveDatabase.saveCurrentTaskLists();
    }


    public void updateTask(Task task) {
    }

    // DELETE the specified task without adding it to the 'completed' tasklists
    public void deleteTask(Task task) {
        ifd("deleteTask: getContent = " + task.getContent());

        TaskList taskList = getCurrentTaskList(task.getParentName());
        taskList.remove(task);
        mDriveDatabase.saveCurrentTaskLists();
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(String taskListName) {
        ifd("removeStruckTasks: " + taskListName);

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
        mDriveDatabase.saveCurrentTaskLists();
        mDriveDatabase.saveHistoricTaskLists();
    }

    private TaskList getHistoricTaskList(String name) {
        // get the TaskList called name from mHistoricTaskLists
        // if it doesn't exist, create it
        List<TaskList> historicTaskLists = mDriveDatabase.getHistoricTaskLists();

        TaskList taskList = getTaskList(historicTaskLists, name);
        if(taskList == null) {
            taskList = new TaskList(0, name);
            historicTaskLists.add(taskList);
        }
        return taskList;
    }

    private TaskList getTaskList(List<TaskList> taskLists, String name) {
        if(taskLists == null) {
            ifd("getTaskList given null taskLists when searching for " + name);
            return null;
        }

        for(TaskList tasklist: taskLists) {
            if(tasklist.getName().equals(name)) {
                return tasklist;
            }
        }

        ifd("unable to find taskList called: " + name);
        return null;
    }

    // return all the tasks associated with the list
    public TaskList getCurrentTaskList(String name) {
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();
        return getTaskList(taskLists, name);
    }

    public TaskList getCurrentTaskList(int index) {
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();

        if(taskLists == null) {
            ifd("mTaskLists is empty");
            return null;
        } else if(index >= taskLists.size()){
            ifd("getCurrentTaskList index is too large");
            return null;
        }
        return taskLists.get(index);
    }

    public List<TaskList> getCurrentTaskLists() {
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();
        return taskLists;
    }

    public void deleteSelectedTaskLists() {
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();
        List<TaskList> removable = new ArrayList<TaskList>();

        for(TaskList taskList: taskLists) {
            if(taskList.isSelected()) {
                removable.add(taskList);
            }
        }

        for(TaskList taskList : removable) {
            taskLists.remove(taskList);
        }

        mDriveDatabase.saveCurrentTaskLists();
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> taskLists = mDriveDatabase.getCurrentTaskLists();

        List<TaskList> res = new ArrayList<TaskList>();
        for(TaskList taskList: taskLists) {
            if(taskList.isDeleteable()) {
                res.add(taskList);
            }
        }
        return res;
    }

    public boolean hasLoadedTaskLists() {
        return mDriveDatabase.hasLoadedTaskLists();
    }

    public void asyncLoadCurrentTaskLists() {
        new TaskListsAsyncTask(mDriveDatabase).execute();
    }

    public void asyncLoadHistoricTaskLists() {
        new HistoricTaskListsAsyncTask(mDriveDatabase).execute();
    }

}
