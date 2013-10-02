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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.indy.octodo.helper.DateFormatHelper;

/*
    A simple immutable structure used to create json
 */
public class TaskListsPack {

    static private final boolean D = true;
    static private final String TAG = "TaskListsPack";

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private static final String HEADER = "header";
    private static final String BODY = "body";

    private static final String DATE_MODIFIED = "date_modified";

    private Date mModifiedDate;
    private List<TaskList> mTaskLists;

    public TaskListsPack(Date modifiedDate, List<TaskList> taskLists) {
        mModifiedDate = modifiedDate;
        mTaskLists = taskLists;
    }

    public Date getModifiedDate() {
        return mModifiedDate;
    }

    public List<TaskList> getTaskLists() {
        return mTaskLists;
    }

    public void setModifiedDate(Date date) {
        mModifiedDate = date;
    }

    public void setTaskLists(List<TaskList> taskLists) {
        mTaskLists.clear();
        mTaskLists.addAll(taskLists);
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();

        try {
            JSONObject header = new JSONObject();
            String today = DateFormatHelper.dateToString(mModifiedDate);
            header.put(DATE_MODIFIED, today);
            res.put(HEADER, header);

            JSONArray array = new JSONArray();
            List<TaskList> taskLists = mTaskLists;
            for (TaskList t : taskLists) {
                JSONObject obj = t.toJson();
                array.put(obj);
            }
            res.put(BODY, array);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static TaskListsPack fromJSON(JSONObject json) {
        Date modified = parseJSONHeader(json);
        List<TaskList> taskLists = parseJSONBody(json);
        TaskListsPack taskListsPack = new TaskListsPack(modified, taskLists);
        return taskListsPack;
    }

    public static Date parseJSONHeader(JSONObject json) {

        String dateString = DateFormatHelper.oldDate();

        try {
            if (json.isNull(HEADER)) {
                ifd("parseJSONHeader: empty header");
            } else {
                JSONObject header = json.getJSONObject(HEADER);
                if (header.has(DATE_MODIFIED)) {
                    dateString = header.getString(DATE_MODIFIED);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return DateFormatHelper.parseDateString(dateString);
    }

    private static List<TaskList> parseJSONBody(JSONObject json) {

        List<TaskList> tasklists = new ArrayList<TaskList>();

        try {

            if (json.isNull(BODY)) {
                // empty body so default to empty today and thisweek tasklists
                ifd("parseJSONBody: empty body so defaulting to empty today and thisweek tasklists");
                return buildDefaultEmptyTaskLists();
            }
            // parse the body which should be a list of tasklists
            //
            JSONArray body = json.getJSONArray(BODY);
            TaskList tasklist;
            for (int i = 0; i < body.length(); i++) {
                tasklist = TaskList.fromJson(body.getJSONObject(i));
                tasklists.add(tasklist);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            ifd("parseJSONBody JSONException: " + e);
            ifd("defaulting to empty today and thisweek tasklists");
            tasklists = buildDefaultEmptyTaskLists();
        }

        return tasklists;
    }

    private static List<TaskList> buildDefaultEmptyTaskLists() {
        List<TaskList> tasklists = new ArrayList<TaskList>();

        TaskList today = new TaskList("today");
        tasklists.add(today);

        TaskList thisWeek = new TaskList("this week");
        tasklists.add(thisWeek);

        return tasklists;
    }

    public static TaskListsPack buildEmptyTaskListsPack() {
        List<TaskList> defaultTaskLists = buildDefaultEmptyTaskLists();
        Date date = new Date(0L);
        return new TaskListsPack(date, defaultTaskLists);
    }
}
