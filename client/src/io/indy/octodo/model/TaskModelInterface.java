package io.indy.octodo.model;

import java.util.List;

public interface TaskModelInterface {

    public void onTaskAdded(Task task);

    public void onTaskUpdateState(Task task, int state);

    public List<Task> onGetTasks(int taskListId);

    public List<TaskList> onGetTaskLists();

    public void onTaskMove(Task task, TaskList destinationTaskList);

    public void onTaskDelete(Task task);
}
