package io.indy.octodo.event;

public class ToggleAddTaskFormEvent  {

    private int mTaskListId;

    public ToggleAddTaskFormEvent(int taskListId) {
        mTaskListId = taskListId;
    }

    public int getTaskListId() {
        return mTaskListId;
    }
}
