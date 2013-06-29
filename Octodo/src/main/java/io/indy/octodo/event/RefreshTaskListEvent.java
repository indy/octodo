
package io.indy.octodo.event;

// event that's broadcast whenever a task is modified
// any UI element that's displaying the task's parent TaskList should refresh

import io.indy.octodo.model.TaskList;

public class RefreshTaskListEvent {

    private TaskList mTaskList;

    public RefreshTaskListEvent(TaskList taskList) {
        mTaskList = taskList;
    }

    public TaskList getTaskList() {
        return mTaskList;
    }
}
