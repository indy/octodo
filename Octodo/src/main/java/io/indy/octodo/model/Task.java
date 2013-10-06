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

import java.util.Comparator;
import java.util.Date;

import io.indy.octodo.AppConfig;
import io.indy.octodo.helper.DateFormatHelper;

// Task specific
//
public class Task {
    static private final boolean D = true;
    static private final String TAG = Task.class.getSimpleName();

    static void ifd(final String message) {
        if (AppConfig.DEBUG && D) Log.d(TAG, message);
    }

    private String mParentName;

    private String mContent;

    private int mState;

    private Date mStartedAt;

    private Date mFinishedAt;

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

        if (mState == STATE_OPEN) {
            mFinishedAt = null;
        } else if (mState == STATE_STRUCK) {
            mFinishedAt = new Date();
        }
    }

    public void setContent(String content) {
        mContent = content;
    }

    public int ageInDays() {
        Date today = new Date();

        Long until = today.getTime();
        Long from = mStartedAt.getTime();

        // convert ms to days
        int days = (int)((until - from) / (1000 * 60 * 60 * 24));

        return days;
    }

    public static Task fromJson(JSONObject jsonObject) {

        try {
            Builder builder = new Builder();
            builder.content(jsonObject.getString(CONTENT));
            String start = jsonObject.getString(STARTED_AT);
            builder.startedAt(DateFormatHelper.parseDateString(start));

            if (jsonObject.has(STATE)) {
                builder.state(jsonObject.getInt(STATE));
            }
            if (jsonObject.has(FINISHED_AT)) {
                String fin = jsonObject.getString(FINISHED_AT);
                builder.finishedAt(DateFormatHelper.parseDateString(fin));
            }

            return builder.build();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();

        try {
            res.put(CONTENT, mContent);
            res.put(STATE, mState);
            res.put(STARTED_AT, DateFormatHelper.dateToString(mStartedAt));
            if (mFinishedAt != null) {
                res.put(FINISHED_AT, DateFormatHelper.dateToString(mFinishedAt));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
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

    public void debug() {
        Log.d(TAG, "idhc: " + System.identityHashCode(this));
        Log.d(TAG, "mContent: " + mContent);
        Log.d(TAG, "mParent: " + mParentName);
        Log.d(TAG, "mState: " + mState);
        Log.d(TAG, "mStartedAt: " + mStartedAt);
        Log.d(TAG, "mFinishedAt: " + mFinishedAt);
    }

    // compare tasks by their age so that TaskLists show the oldest tasks at top
    //
    public static class CompareByAge implements Comparator<Task> {
        public int compare(Task t1, Task t2) {
            return t1.mStartedAt.compareTo(t2.mStartedAt);
        }
    }

    public static class Builder {
        private String mContent = "";

        private int mState = 0;

        private Date mStartedAt = new Date();

        private Date mFinishedAt = null;

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

        public Builder startedAt(Date val) {
            mStartedAt = val;
            return this;
        }

        public Builder finishedAt(Date val) {
            mFinishedAt = val;
            return this;
        }

        public Task build() {
            return new Task(this);
        }

    }

}
