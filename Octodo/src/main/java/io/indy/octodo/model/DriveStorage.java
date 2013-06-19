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

public class DriveStorage {

    /*
    re-implement the public methods from Database
    store an instance of this class in MainController
    (maybe store MainController in Application?)
     */

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;


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

    public void addTask(Task task) {
    }

    public void updateTaskContent(int taskId, String content) {
        if (D) {
            Log.d(TAG, "updateTaskContent id: " + taskId + " content: " + content);
        }
    }

    public void updateTaskState(int taskId, int state) {
        if (D) {
            Log.d(TAG, "updateTaskState id:" + taskId + " state: " + state);
        }
    }

    // re-assign a task to a different tasklist
    public void updateTaskParentList(int taskId, int taskListId) {
        if (D) {
            Log.d(TAG, "updateTaskParentList taskId: " + taskId + " taskListId: " + taskListId);
        }
    }


    public void updateTask(Task task) {
    }

    // DELETE the specified task
    public void deleteTask(int taskId) {
        if (D) {
            Log.d(TAG, "deleteTask: taskId=" + taskId);
        }
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(int taskListId) {
        if (D) {
            Log.d(TAG, "removeStruckTasks: taskListId=" + taskListId);
        }
    }

    // return all the tasks associated with the list
    public List<Task> getTasks(int taskListId) {
        List<Task> res = new ArrayList<Task>();
        return res;
    }

    public List<TaskList> getTaskLists() {
        List<TaskList> res = new ArrayList<TaskList>();
        return res;
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> res = new ArrayList<TaskList>();
        return res;
    }

}
