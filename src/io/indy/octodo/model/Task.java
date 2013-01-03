package io.indy.octodo.model;

// Task specific
//
public class Task {
    private final int mId;
    private final int mListId;
    private final String mContent;
    private final int mState;

    public Task(int id, int taskListId, String content, int state) {
        mId = id;
        mListId = taskListId;
        mContent = content;
        mState = state;
    }
}
