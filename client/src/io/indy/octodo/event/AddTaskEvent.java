package io.indy.octodo.event;

import io.indy.octodo.model.Task;

public class AddTaskEvent  {

    private Task mTask;

    public AddTaskEvent(Task task) {
        mTask = task;
    }

    public int getTaskListId() {
        return mTask.getListId();
    }
}
