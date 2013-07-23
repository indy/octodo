/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.indy.octodo.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.indy.octodo.helper.DateFormatHelper;

// Task specific
//
public class Task {
    static private final boolean D = true;
    static private final String TAG = Task.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private String mParentName;

    private String mContent;

    private int mState;

    private String mStartedAt;

    private String mFinishedAt;

    public static final int STATE_OPEN = 0;

    public static final int STATE_STRUCK = 1;

    public static final int STATE_CLOSED = 2;

    private static final String CONTENT = "content";
    private static final String STATE = "state";
    private static final String STARTED_AT = "started_at";
    private static final String FINISHED_AT = "finished_at";

    private Task(Builder builder) {
        mContent = builder.mContent;
        mState = builder.mState;
        mStartedAt = builder.mStartedAt;
        mFinishedAt = builder.mFinishedAt;
    }

    public void setParentName(String name) {
        mParentName = name;
    }

    public String getParentName() {
        return mParentName;
    }

    public void setState(int state) {
        mState = state;

        if(mState == STATE_OPEN) {
            mFinishedAt = "";
        } else if(mState == STATE_STRUCK) {
            mFinishedAt = DateFormatHelper.today();
        }
    }

    public void setContent(String content) {
        mContent = content;
    }

    public static Task fromJson(JSONObject jsonObject) {

        try {
            Builder builder = new Builder()
                    .content(jsonObject.getString(CONTENT))
                    .startedAt(jsonObject.getString(STARTED_AT));

            if(jsonObject.has(STATE)) {
                builder.state(jsonObject.getInt(STATE));
            }
            if(jsonObject.has(FINISHED_AT)) {
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
            res.put(CONTENT, mContent);
            res.put(STATE, mState);
            res.put(STARTED_AT, mStartedAt);
            if(!mFinishedAt.isEmpty()) {
                res.put(FINISHED_AT, mFinishedAt);
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }

        return res;
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
        private String mContent = "";

        private int mState = 0;

        private String mStartedAt = "";

        private String mFinishedAt = "";

        public Builder() {
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
