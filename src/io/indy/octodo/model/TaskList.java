package io.indy.octodo.model;

public class TaskList {
    private final int mId;
    private final String mName;

    // set by ManageListsAdapter
    private boolean mIsSelected;

    public static final int STATE_ACTIVE = 0;
    public static final int STATE_INACTIVE = 1;

    public TaskList(int id, String name) {
        mId = id;
        mName = name;
        mIsSelected = false;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    @Override
    public String toString() {
        return mName;
    }


}
