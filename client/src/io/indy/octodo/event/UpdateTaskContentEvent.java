package io.indy.octodo.event;

import io.indy.octodo.model.Task;

public class UpdateTaskContentEvent  {

    private Task mTask;

    public UpdateTaskContentEvent(Task task) {
        mTask = task;
    }

    public int getTaskListId() {
        return mTask.getListId();
    }
}
