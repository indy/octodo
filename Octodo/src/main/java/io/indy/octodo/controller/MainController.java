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
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.DriveModel;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class MainController {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private Activity mActivity;

    private NotificationHelper mNotification;

    private DriveModel mDriveModel;

    public MainController(Activity activity, DriveModel driveModel) {
        mActivity = activity;
        mNotification = new NotificationHelper(activity);
        mDriveModel = driveModel;
    }

    public void onTaskAdd(Task task, String taskListName) {
        mDriveModel.addTask(task, taskListName);
        postRefreshEvent(taskListName);
    }

    public void onTaskUpdateContent(Task task, String content) {
        mDriveModel.updateTaskContent(task, content);
        postRefreshEvent(task.getParentName());
    }

    public void onTaskUpdateState(Task task, int state) {
        mDriveModel.updateTaskState(task, state);
        postRefreshEvent(task.getParentName());
    }

    public TaskList onGetTaskList(String name) {
        return mDriveModel.getCurrentTaskList(name);
    }

    public TaskList onGetTaskList(int index) {
        return mDriveModel.getCurrentTaskList(index);
    }

    public List<TaskList> onGetTaskLists() {
        return mDriveModel.getCurrentTaskLists();
    }

    public void onTaskMove(Task task, String destinationTaskList) {
        mDriveModel.moveTask(task, destinationTaskList);

        post(new MoveTaskEvent(task, task.getParentName(), destinationTaskList));

        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList + "\"");
    }

    public void onTaskDelete(Task task) {
        String parentName = task.getParentName();
        mDriveModel.deleteTask(task);
        postRefreshEvent(parentName);
    }

    public void onRemoveCompletedTasks(String taskListName) {

        mDriveModel.removeStruckTasks(taskListName);
        postRefreshEvent(taskListName);

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskListName + "\"");
    }

    public void deleteSelectedTaskLists() {
        mDriveModel.deleteSelectedTaskLists();
    }

    public void addList(String name) {
        // check if a tasklist with this name already exists
        mDriveModel.addList(name);
    }

    public List<TaskList> getDeleteableTaskLists() {
        return mDriveModel.getDeleteableTaskLists();
    }

    public void onDestroy() {
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
