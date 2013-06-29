
package io.indy.octodo.controller;

import io.indy.octodo.R;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.SQLDatabase;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

import java.util.List;

import android.app.Activity;
import de.greenrobot.event.EventBus;

public class MainController {

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

    public void onTaskAdd(TaskList parentTaskList, Task task) {
        //mSQLDatabase.addTask(task);
        mDriveDatabase.addTask(parentTaskList, task);

        postRefreshEvent(parentTaskList);
    }

    public void onTaskUpdateContent(Task task, String content) {
        int taskId = task.getId();
        mSQLDatabase.updateTaskContent(taskId, content);

        postRefreshEvent(task.getParentTaskList());
    }

    public void onTaskUpdateState(Task task, int state) {
        int taskId = task.getId();
        mSQLDatabase.updateTaskState(taskId, state);

        postRefreshEvent(task.getParentTaskList());
    }

    // updated
    public TaskList onGetTaskList(String name) {
        return mDriveDatabase.getTaskList(name);
        // return mSQLDatabase.getTasks(taskListId);
    }

    // updated
    public List<TaskList> onGetTaskLists() {
        return mDriveDatabase.getTaskLists();
        //return mSQLDatabase.getTaskLists();
    }

    public void onTaskMove(Task task, TaskList destinationTaskList) {
        int newTaskListId = destinationTaskList.getId();
        int oldTaskListId = task.getListId();

        // update model
        mSQLDatabase.updateTaskParentList(task.getId(), newTaskListId);

        // update ui
        post(new MoveTaskEvent(task, task.getParentTaskList(), destinationTaskList));

        // show crouton
        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList.getName() + "\"");

    }

    public void onTaskDelete(Task task) {
        mSQLDatabase.deleteTask(task.getId());

        postRefreshEvent(task.getParentTaskList());
    }

    public void onRemoveCompletedTasks(TaskList taskList) {
        mSQLDatabase.removeStruckTasks(taskList.getId());

        // update UI (via TaskListFragment)
        postRefreshEvent(taskList);

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskList.getName() + "\"");
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

    private void postRefreshEvent(TaskList taskList) {
        post(new RefreshTaskListEvent(taskList));
    }
}
