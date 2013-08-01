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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.view.TaskItemView;

public class TaskItemAdapter extends ArrayAdapter<Task> {

    static private final boolean D = false;
    static private final String TAG = TaskItemAdapter.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private Fragment mFragment;

    private final Context mContext;

    private MainController mController;

    public TaskItemAdapter(Context context, TaskList taskList, MainController controller, Fragment fragment) {
        super(context,
                android.R.layout.simple_list_item_1,
                taskList.getTasks());

        mController = controller;
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ifd("getView position: " + position);

        Task task = getItem(position);
        TaskItemView taskItemView = (TaskItemView) v;

        if (taskItemView == null) {
            taskItemView = new TaskItemView(mContext);
            taskItemView.setController(mController);
            taskItemView.setFragment(mFragment);
        }

        taskItemView.setupWithTask(task);

        return taskItemView;
    }

}
