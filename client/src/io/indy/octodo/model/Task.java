package io.indy.octodo.model;

// Task specific
//
public class Task {
    private int mId;
    private int mListId;
    private String mContent;
    private int mState;

    private String mStartedAt;
    private String mFinishedAt;

    public static final int STATE_OPEN = 0;
    public static final int STATE_STRUCK = 1;
    public static final int STATE_CLOSED = 2;

    private Task(Builder builder) {
        mId = builder.mId;
        mListId = builder.mListId;
        mContent = builder.mContent;
        mState = builder.mState;
        mStartedAt = builder.mStartedAt;
        mFinishedAt = builder.mFinishedAt;
    }

    public int getId() {
        return mId;
    }

    public int getListId() {
        return mListId;
    }

    public String getContent() {
        return mContent;
    }

    public int getState() {
        return mState;
    }

    public String getStartedAt() {
        return mStartedAt;
    }

    public String getFinishedAt() {
        return mFinishedAt;
    }

    
    @Override
    public String toString() {
        return mContent;
    }


    public static class Builder {
        private int mId = 0;
        private int mListId = 0;
        private String mContent = "";
        private int mState = 0;
        private String mStartedAt = "";
        private String mFinishedAt = "";

        public Builder() {

        }

        public Builder id(int val) {
            mId = val;
            return this;
        }

        public Builder listId(int val) {
            mListId = val;
            return this;
        }

        public Builder content(String val) {
            mContent = val;
            return this;
        }

        public Builder state(int val) {
            mState = val;
            return this;
        }

        public Builder startedAt(String val) {
            mStartedAt = val;
            return this;
        }

        public Builder finishedAt(String val) {
            mFinishedAt = val;
            return this;
        }

        public Task build() {
            return new Task(this);
        }

    }

}
