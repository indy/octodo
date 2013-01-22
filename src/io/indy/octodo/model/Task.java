package io.indy.octodo.model;

// Task specific
//
public class Task {
    private int mId;
    private int mListId;
    private String mContent;
    private int mState;

    public static final int STATE_OPEN = 0;
    public static final int STATE_STRUCK = 1;
    public static final int STATE_CLOSED = 2;

    private Task(Builder builder) {
        mId = builder.mId;
        mListId = builder.mListId;
        mContent = builder.mContent;
        mState = builder.mState;
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
    
    @Override
    public String toString() {
        return mContent;
    }


    public static class Builder {
        private int mId = 0;
        private int mListId = 0;
        private String mContent = "";
        private int mState = 0;

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

        public Task build() {
            return new Task(this);
        }

    }

}
