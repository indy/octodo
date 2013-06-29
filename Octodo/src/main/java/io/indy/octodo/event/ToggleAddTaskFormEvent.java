
package io.indy.octodo.event;

import io.indy.octodo.model.TaskList;

public class ToggleAddTaskFormEvent {

    private TaskList mTaskList;

    public ToggleAddTaskFormEvent(TaskList taskList) {
        mTaskList = taskList;
    }

    public TaskList getTaskList() {
        return mTaskList;
    }
}
