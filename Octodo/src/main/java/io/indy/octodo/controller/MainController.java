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
import android.util.Log;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.R;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggledTaskStateEvent;
import io.indy.octodo.helper.DateFormatHelper;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class MainController {

    static private final boolean D = true;
    static private final String TAG = MainController.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private Activity mActivity;

    private NotificationHelper mNotification;

    private OctodoModel mOctodoModel;

    public MainController(Activity activity, OctodoModel octodoModel) {
        mActivity = activity;
        mNotification = new NotificationHelper(activity);
        mOctodoModel = octodoModel;
    }

    public Task findTask(String listName, String startedAt) {
        TaskList taskList = mOctodoModel.getCurrentTaskList(listName);
        for (Task t : taskList.getTasks()) {
            if (startedAt.equals(t.getStartedAt())) {
                return t;
            }
        }

        Log.e(TAG, "unable to find task in " + listName + " with started at value of " + startedAt);
        return null;
    }

    public void onTaskAdd(String content, String taskListName) {
        ifd("onTaskAdd [" + taskListName + "] " + content);

        content = content.trim();
        if (content.length() == 0) {
            // don't add empty strings
            return;
        }

        Task task = new Task.Builder()
                .content(content)
                .state(0)
                .startedAt(DateFormatHelper.today())
                .build();

        mOctodoModel.addTask(task, taskListName);
        postRefreshEvent(taskListName);
    }

    public void onTaskUpdateState(Task task, int state) {
        mOctodoModel.updateTaskState(task, state);
        // post a ToggledTaskStateEvent so that the activity can show/hide trashcan icon
        post(new ToggledTaskStateEvent(task.getParentName()));
    }

    public TaskList onGetTaskList(String name) {
        return mOctodoModel.getCurrentTaskList(name);
    }

    public TaskList onGetTaskList(int index) {
        return mOctodoModel.getCurrentTaskList(index);
    }

    public List<TaskList> onGetTaskLists() {
        return mOctodoModel.getCurrentTaskLists();
    }

    public void onTaskUpdateContent(Task task, String content) {
        mOctodoModel.updateTaskContent(task, content);
        postRefreshEvent(task.getParentName());
    }

    public void onTaskMove(Task task, String newTaskList) {
        String oldTaskList = task.getParentName();
        mOctodoModel.updateTaskParent(task, newTaskList);
        postRefreshEvent(oldTaskList);
        postRefreshEvent(newTaskList);
    }

    public void onTaskDelete(Task task) {
        String parentName = task.getParentName();
        mOctodoModel.deleteTask(task);
        postRefreshEvent(parentName);
    }

    public void onRemoveCompletedTasks(String taskListName) {
        mOctodoModel.removeStruckTasks(taskListName);
        postRefreshEvent(taskListName);
        String messagePrefix = mActivity.getString(R.string.confirmation_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskListName + "\"");
    }

    public void deleteSelectedTaskLists() {
        mOctodoModel.deleteSelectedTaskLists();
    }

    public void addList(String name) {
        // check if a tasklist with this name already exists
        mOctodoModel.addList(name);
    }

    public List<TaskList> getDeleteableTaskLists() {
        return mOctodoModel.getDeleteableTaskLists();
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
