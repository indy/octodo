package io.indy.octodo.model;

import java.util.List;

public interface TaskModelInterface {

    public void onTaskAdded(Task newTask);

    public void onTaskUpdateState(int taskId, int state);

    public List<Task> onGetTasks(int taskListId);

    public void onTaskDelete(Task task);
}
