package io.indy.octodo.event;

import io.indy.octodo.model.Task;

public class MoveTaskEvent  {

    private Task mTask;
    private int mOldTaskListId;
    private int mNewTaskListId;

    public MoveTaskEvent(Task task, int oldTaskListId, int newTaskListId) {
        mTask = task;
        mOldTaskListId = oldTaskListId;
        mNewTaskListId = newTaskListId;
    }

    public int getOldTaskListId() {
        return mOldTaskListId;
    }

    public int getNewTaskListId() {
        return mNewTaskListId;
    }
}
