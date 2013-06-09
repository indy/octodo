
package io.indy.octodo.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

// Task specific
//
public class Task {

    private static final String TAG = "Task";
    private static final boolean D = true;

    private int mId;

    private int mListId;

    private String mContent;

    private int mState;

    private String mStartedAt;

    private String mFinishedAt;

    public static final int STATE_OPEN = 0;

    public static final int STATE_STRUCK = 1;

    public static final int STATE_CLOSED = 2;

    private static final String ID = "id";
    private static final String LIST_ID = "list_id";
    private static final String CONTENT = "content";
    private static final String STARTED_AT = "started_at";
    private static final String FINISHED_AT = "finished_at";

    private Task(Builder builder) {
        mId = builder.mId;
        mListId = builder.mListId;
        mContent = builder.mContent;
        mState = builder.mState;
        mStartedAt = builder.mStartedAt;
        mFinishedAt = builder.mFinishedAt;
    }

    public static Task fromJson(JSONObject jsonObject) {

        try {
            Builder builder = new Builder()
                    .id(jsonObject.getInt(ID))
                    .listId(jsonObject.getInt(LIST_ID))
                    .content(jsonObject.getString(CONTENT))
                    .startedAt(jsonObject.getString(STARTED_AT));

            if(jsonObject.get(FINISHED_AT) != JSONObject.NULL) {
                builder.finishedAt(jsonObject.getString(FINISHED_AT));
            }

            return builder.build();

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return null;
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();

        try {
            res.put(ID, mId);
            res.put(LIST_ID, mListId);
            res.put(CONTENT, mContent);
            res.put(STARTED_AT, mStartedAt);
            res.put(FINISHED_AT, mFinishedAt);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return res;
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
