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

package io.indy.octodo.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import java.util.List;

import io.indy.octodo.AppConfig;
import io.indy.octodo.R;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.view.TaskItemView;

public class TasksAdapter extends BaseAdapter {

    static private final boolean D = false;
    static private final String TAG = TasksAdapter.class.getSimpleName();

    static void ifd(final String message) {
        if (AppConfig.DEBUG && D) Log.d(TAG, message);
    }

    private final int VIEW_TYPE_TASK = 1;
    private final int VIEW_TYPE_FORM = 0;

    private Fragment mFragment;

    private final Context mContext;

    private MainController mController;

    private TaskList mTaskList;

    public TasksAdapter(Context context, TaskList taskList, MainController controller, Fragment fragment) {
        mController = controller;
        mContext = context;
        mFragment = fragment;
        mTaskList = taskList;
    }

    @Override
    public int getCount() {
        // number of tasks + the 'add new task' form
        return mTaskList.getTasks().size() + 1;
    }

    @Override
    public Object getItem(int position) {

        List<Task> tasks = mTaskList.getTasks();

        if (position == tasks.size()) {
            // return something to represent the 'add new task' form
            return Integer.valueOf(42);
        } else if (position < tasks.size()) {
            return tasks.get(position);
        }

        throw new IllegalArgumentException("Invalid position: " + String.valueOf(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Integer) {
            // Integer == show the 'add new task' form
            return VIEW_TYPE_FORM;
        }
        // it's a task
        return VIEW_TYPE_TASK;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == VIEW_TYPE_FORM) {
            return getFormView(position, convertView, parent);
        }

        Task task = (Task) getItem(position);
        TaskItemView taskItemView = (TaskItemView) convertView;

        if (taskItemView == null) {
            taskItemView = new TaskItemView(mContext);
            taskItemView.setController(mController);
            taskItemView.setFragment(mFragment);
        }

        taskItemView.setupWithTask(task);

        return taskItemView;
    }

    private View getFormView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            String infService = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li;
            li = (LayoutInflater) mContext.getSystemService(infService);
            row = li.inflate(R.layout.row_add_task, parent, false);
        }

        ifd("getFormView " + mTaskList.getName());

        final EditText editText = (EditText) row.findViewById(R.id.editTextTask);
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66 && event.getAction() == 1) {
                    String content = editText.getText().toString();
                    editText.setText("");

                    mController.onTaskAdd(content, mTaskList.getName());
                }
                return false;
            }
        });

        return row;
    }
}
