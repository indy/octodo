
package io.indy.octodo.event;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class MoveTaskEvent {

    private Task mTask;

    private TaskList mOldTaskList;

    private TaskList mNewTaskList;

    public MoveTaskEvent(Task task, TaskList oldTaskList, TaskList newTaskList) {
        mTask = task;
        mOldTaskList = oldTaskList;
        mNewTaskList = newTaskList;
    }

    public TaskList getOldTaskList() {
        return mOldTaskList;
    }

    public TaskList getNewTaskList() {
        return mNewTaskList;
    }
}
