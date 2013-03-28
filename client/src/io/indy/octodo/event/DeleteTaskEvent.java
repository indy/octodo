package io.indy.octodo.event;

import io.indy.octodo.model.Task;

public class DeleteTaskEvent  {

    private Task mTask;

    public DeleteTaskEvent(Task task) {
        mTask = task;
    }

    public int getTaskListId() {
        return mTask.getListId();
    }
}
