
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

    public void onTaskUpdateContent(Task task, String taskListName, String content) {
        mDriveDatabase.updateTaskContent(task, content);
        postRefreshEvent(taskListName);
    }

    public void onTaskUpdateState(Task task, String taskListName, int state) {
        mDriveDatabase.updateTaskState(task, state);
        postRefreshEvent(taskListName);
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

    public void onTaskMove(Task task, String sourceTaskList, String destinationTaskList) {
        mDriveDatabase.moveTask(task, sourceTaskList, destinationTaskList);

        post(new MoveTaskEvent(task, sourceTaskList, destinationTaskList));

        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList + "\"");
    }

    public void onTaskDelete(Task task, String taskListName) {
        mDriveDatabase.deleteTask(task, taskListName);
        postRefreshEvent(taskListName);
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

    public void deleteList(int id) {
        mSQLDatabase.deleteList(id);
    }

    public void addList(String name) {
        mSQLDatabase.addList(name);
    }

    public List<TaskList> getDeleteableTaskLists() {
        return mSQLDatabase.getDeleteableTaskLists();
    }

    public void onDestroy() {
        mSQLDatabase.closeDatabase();
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
