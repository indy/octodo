
package io.indy.octodo.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

// Task specific
//
public class Task {

    private final String TAG = getClass().getSimpleName();
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



    public static final String ID = "id";
    public static final String LIST_ID = "list_id";
    public static final String CONTENT = "content";
    public static final String STARTED_AT = "started_at";
    public static final String FINISHED_AT = "finished_at";

    private Task(Builder builder) {
        mId = builder.mId;
        mListId = builder.mListId;
        mContent = builder.mContent;
        mState = builder.mState;
        mStartedAt = builder.mStartedAt;
        mFinishedAt = builder.mFinishedAt;
    }

    public static Task fromJson(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);

            int id = jsonObject.getInt(ID);
            int listId = jsonObject.getInt(LIST_ID);
            String content = jsonObject.getString(CONTENT);
            String startedAt = jsonObject.getString(STARTED_AT);

            Builder builder = new Builder()
                    .id(id)
                    .listId(listId)
                    .content(content)
                    .state(0)
                    .startedAt(startedAt);

            if(jsonObject.get(FINISHED_AT) != JSONObject.NULL) {
                String finishedAt = jsonObject.getString(FINISHED_AT);
                builder.finishedAt(finishedAt);
            }

            Task task = builder.build();
            return task;

        } catch (JSONException e) {

        }

        return null;
    }

    public String toJson() {
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

        return res.toString();
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
