package io.indy.octodo.event;

public class RemoveCompletedTasksEvent  {

    private int mTaskListId;

    public RemoveCompletedTasksEvent(int taskListId) {
        mTaskListId = taskListId;
    }

    public int getTaskListId() {
        return mTaskListId;
    }
}
