package io.indy.octodo.controller;

import io.indy.octodo.R;
import io.indy.octodo.event.AddTaskEvent;
import io.indy.octodo.event.DeleteTaskEvent;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RemoveCompletedTasksEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.event.UpdateTaskStateEvent;
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

        AddTaskEvent event;
        event = new AddTaskEvent(task);
        EventBus.getDefault().post(event);
    }

    public void onTaskUpdateState(Task task, int state) {
        int taskId = task.getId();
        mDatabase.updateTaskState(taskId, state);

        UpdateTaskStateEvent event;
        event = new UpdateTaskStateEvent(task, state);
        EventBus.getDefault().post(event);
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
        MoveTaskEvent moveEvent;
        moveEvent = new MoveTaskEvent(task, oldTaskListId, newTaskListId);
        EventBus.getDefault().post(moveEvent);

        // show crouton
        String messagePrefix = mActivity.getString(R.string.notification_moved_task);
        notifyUser(messagePrefix + " \"" + destinationTaskList.getName()
                + "\"");

    }

    public void onTaskDelete(Task task) {
        mDatabase.deleteTask(task.getId());

        DeleteTaskEvent dtEvent;
        dtEvent = new DeleteTaskEvent(task);
        EventBus.getDefault().post(dtEvent);
    }

    public void onRemoveCompletedTasks(TaskList taskList) {
        mDatabase.removeStruckTasks(taskList.getId());

        // update UI (via TaskListFragment)
        RemoveCompletedTasksEvent rctEvent;
        rctEvent = new RemoveCompletedTasksEvent(taskList.getId());
        EventBus.getDefault().post(rctEvent);

        // show Crouton
        String messagePrefix = mActivity.getString(R.string.notification_remove_completed_tasks);
        notifyUser(messagePrefix + " \"" + taskList.getName() + "\"");
    }

    public void onToggleAddTaskForm(int taskListId) {
        ToggleAddTaskFormEvent tatfEvent;
        tatfEvent = new ToggleAddTaskFormEvent(taskListId);
        EventBus.getDefault().post(tatfEvent);
    }

    private void notifyUser(String message) {
        NotificationHelper nh = new NotificationHelper(mActivity);
        nh.showConfirmation(message);
    }

}
