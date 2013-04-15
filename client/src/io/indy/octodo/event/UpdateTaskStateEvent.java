package io.indy.octodo.event;

import io.indy.octodo.model.Task;

public class UpdateTaskStateEvent  {

    private Task mTask;
    private int mNewState;

    public UpdateTaskStateEvent(Task task, int newState) {
        mTask = task;
        mNewState = newState;
    }

    public int getTaskListId() {
        return mTask.getListId();
    }
}
