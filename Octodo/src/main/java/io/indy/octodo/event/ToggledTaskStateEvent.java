package io.indy.octodo.event;

public class ToggledTaskStateEvent {

    private String mTaskListName;

    public ToggledTaskStateEvent(String taskListName) {
        mTaskListName = taskListName;
    }

    public String getTaskListName() {
        return mTaskListName;
    }
}
