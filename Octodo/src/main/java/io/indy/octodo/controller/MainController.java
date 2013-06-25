
package io.indy.octodo.controller;

import io.indy.octodo.R;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.NotificationHelper;
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

    public MainController(Activity activity) {
        mActivity = activity;
        mSQLDatabase = new SQLDatabase(activity);
        mNotification = new NotificationHelper(activity);
    }

    public void onTaskAdded(Task task) {
        mSQLDatabase.addTask(task);

        postRefreshEvent(task.getListId());
    }

    public void onTaskUpdateContent(Task task, String content) {
        int taskId = task.getId();
        mSQLDatabase.updateTaskContent(taskId, content);

        postRefreshEvent(task.getListId());
    }

    public void onTaskUpdateState(Task task, int state) {
        int taskId = task.getId();
        mSQLDatabase.updateTaskState(taskId, state);

        postRefreshEvent(task.getListId());
    }

    public List<Task> onGetTasks(int taskListId) {
        return mSQLDatabase.getTasks(taskListId);
    }

    public List<TaskList> onGetTaskLists() {
        return mSQLDatabase.getTaskLists();
    }

    public void onTaskMove(Task task, TaskList destinationTaskList) {
        int newTaskListId = destinationTaskList.getId();
        int oldTaskListId = task.getListId();

        // update model
        mSQLDatabase.updateTaskParentList(task.getId(), newTaskListId);

        // update ui
        post(new MoveTaskEvent(task, oldTaskListId, newTaskListId));

        // show crouton
        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList.getName() + "\"");

    }

    public void onTaskDelete(Task task) {
        mSQLDatabase.deleteTask(task.getId());

        postRefreshEvent(task.getListId());
    }

    public void onRemoveCompletedTasks(TaskList taskList) {
        mSQLDatabase.removeStruckTasks(taskList.getId());

        // update UI (via TaskListFragment)
        postRefreshEvent(taskList.getId());

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskList.getName() + "\"");
    }

    public void onToggleAddTaskForm(int taskListId) {
        post(new ToggleAddTaskFormEvent(taskListId));
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

    private void postRefreshEvent(int taskListId) {
        post(new RefreshTaskListEvent(taskListId));
    }
}
