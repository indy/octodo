package io.indy.octodo.model;

// Task specific
//
public class Task {
    private final int mId;
    private final int mListId;
    private final String mContent;
    private final int mState;

    public Task(int listId, String content) {
        mId = 0;
        mListId = listId;
        mContent = content;
        mState = 0;
    }
}
