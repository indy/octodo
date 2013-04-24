package io.indy.octodo.event;

// event that's broadcast whenever a task is modified
// any UI element that's displaying the task's parent TaskList should refresh

public class RefreshTaskListEvent  {

    private int mTaskListId;

    public RefreshTaskListEvent(int taskListId) {
        mTaskListId = taskListId;
    }

    public int getTaskListId() {
        return mTaskListId;
    }
}
