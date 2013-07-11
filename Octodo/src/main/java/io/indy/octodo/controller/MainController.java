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

package io.indy.octodo.controller;

import android.app.Activity;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.R;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.SQLDatabase;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class MainController {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private Activity mActivity;

    private SQLDatabase mSQLDatabase;

    private NotificationHelper mNotification;

    private DriveDatabase mDriveDatabase;

    public MainController(Activity activity, DriveDatabase driveDatabase) {
        mActivity = activity;
        mSQLDatabase = new SQLDatabase(activity);
        mNotification = new NotificationHelper(activity);
        mDriveDatabase = driveDatabase;
    }

    public void onTaskAdd(Task task, String taskListName) {
        mDriveDatabase.addTask(task, taskListName);
        postRefreshEvent(taskListName);
    }

    public void onTaskUpdateContent(Task task, String content) {
        mDriveDatabase.updateTaskContent(task, content);
        postRefreshEvent(task.getParentName());
    }

    public void onTaskUpdateState(Task task, int state) {
        mDriveDatabase.updateTaskState(task, state);
        postRefreshEvent(task.getParentName());
    }

    public TaskList onGetTaskList(String name) {
        return mDriveDatabase.getCurrentTaskList(name);
    }

    public TaskList onGetTaskList(int index) {
        return mDriveDatabase.getCurrentTaskList(index);
    }

    public List<TaskList> onGetTaskLists() {
        return mDriveDatabase.getCurrentTaskLists();
    }

    public void onTaskMove(Task task, String destinationTaskList) {
        mDriveDatabase.moveTask(task, destinationTaskList);

        post(new MoveTaskEvent(task, task.getParentName(), destinationTaskList));

        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList + "\"");
    }

    public void onTaskDelete(Task task) {
        String parentName = task.getParentName();
        mDriveDatabase.deleteTask(task);
        postRefreshEvent(parentName);
    }

    public void onRemoveCompletedTasks(String taskListName) {

        mDriveDatabase.removeStruckTasks(taskListName);
        postRefreshEvent(taskListName);

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskListName + "\"");
    }

    public void onToggleAddTaskForm(TaskList taskList) {
        post(new ToggleAddTaskFormEvent(taskList));
    }

    public void closeDatabase() {
        mSQLDatabase.closeDatabase();
    }

    public boolean deleteList(String name) {
        return mDriveDatabase.deleteList(name);
    }

    public void addList(String name) {
        // check if a tasklist with this name already exists
        mDriveDatabase.addList(name);
    }

    public List<TaskList> getDeleteableTaskLists() {
        return mDriveDatabase.getDeleteableTaskLists();
    }

    public void onDestroy() {
        //mSQLDatabase.closeDatabase();
        mNotification.cancelAllNotifications();
    }

    private void notifyUser(String message) {
        mNotification.showConfirmation(message);
    }

    private void post(Object event) {
        EventBus.getDefault().post(event);
    }

    private void postRefreshEvent(String taskListName) {
        post(new RefreshTaskListEvent(taskListName));
    }
}
