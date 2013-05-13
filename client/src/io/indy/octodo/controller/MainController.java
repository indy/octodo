
package io.indy.octodo.controller;

import io.indy.octodo.R;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.Database;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

import java.util.List;

import android.app.Activity;
import de.greenrobot.event.EventBus;

public class MainController {

    private Activity mActivity;

    private Database mDatabase;

    public MainController(Activity activity, Database database) {
        mActivity = activity;
        mDatabase = database;
    }

    public void onTaskAdded(Task task) {
        mDatabase.addTask(task);

        postRefreshEvent(task.getListId());
    }

    public void onTaskUpdateContent(Task task, String content) {
        int taskId = task.getId();
        mDatabase.updateTaskContent(taskId, content);

        postRefreshEvent(task.getListId());
    }

    public void onTaskUpdateState(Task task, int state) {
        int taskId = task.getId();
        mDatabase.updateTaskState(taskId, state);

        postRefreshEvent(task.getListId());
    }

    public List<Task> onGetTasks(int taskListId) {
        return mDatabase.getTasks(taskListId);
    }

    public List<TaskList> onGetTaskLists() {
        return mDatabase.getTaskLists();
    }

    public void onTaskMove(Task task, TaskList destinationTaskList) {
        int newTaskListId = destinationTaskList.getId();
        int oldTaskListId = task.getListId();

        // update model
        mDatabase.updateTaskParentList(task.getId(), newTaskListId);

        // update ui
        post(new MoveTaskEvent(task, oldTaskListId, newTaskListId));

        // show crouton
        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList.getName() + "\"");

    }

    public void onTaskDelete(Task task) {
        mDatabase.deleteTask(task.getId());

        postRefreshEvent(task.getListId());
    }

    public void onRemoveCompletedTasks(TaskList taskList) {
        mDatabase.removeStruckTasks(taskList.getId());

        // update UI (via TaskListFragment)
        postRefreshEvent(taskList.getId());

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskList.getName() + "\"");
    }

    public void onToggleAddTaskForm(int taskListId) {
        post(new ToggleAddTaskFormEvent(taskListId));
    }

    private void notifyUser(String message) {
        NotificationHelper nh = new NotificationHelper(mActivity);
        nh.showConfirmation(message);
    }

    private void post(Object event) {
        EventBus.getDefault().post(event);
    }

    private void postRefreshEvent(int taskListId) {
        post(new RefreshTaskListEvent(taskListId));
    }

    public void cancelAllNotifications() {
        NotificationHelper.cancelAllNotifications();
    }

}
