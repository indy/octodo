
package io.indy.octodo.event;

// event that's broadcast whenever a task is modified
// any UI element that's displaying the task's parent TaskList should refresh

public class RefreshTaskListEvent {

    private String mTaskListName;

    public RefreshTaskListEvent(String taskListName) {
        mTaskListName = taskListName;
    }

    public String getTaskListName() {
        return mTaskListName;
    }
}
