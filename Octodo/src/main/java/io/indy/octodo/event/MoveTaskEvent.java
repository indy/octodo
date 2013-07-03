
package io.indy.octodo.event;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class MoveTaskEvent {

    private Task mTask;

    private String mOldTaskList;

    private String mNewTaskList;

    public MoveTaskEvent(Task task, String oldTaskList, String newTaskList) {
        mTask = task;
        mOldTaskList = oldTaskList;
        mNewTaskList = newTaskList;
    }

    public String getOldTaskList() {
        return mOldTaskList;
    }

    public String getNewTaskList() {
        return mNewTaskList;
    }
}
