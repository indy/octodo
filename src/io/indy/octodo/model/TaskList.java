package io.indy.octodo.model;

public class TaskList {
    private final int mId;
    private final String mName;

    public TaskList(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
}