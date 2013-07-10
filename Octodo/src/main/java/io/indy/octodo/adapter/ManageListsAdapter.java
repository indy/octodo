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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import io.indy.octodo.R;
import io.indy.octodo.model.TaskList;

public class ManageListsAdapter extends ArrayAdapter<TaskList> implements OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private final LayoutInflater mInflater;

    public ManageListsAdapter(Context context, List<TaskList> items) {
        super(context, android.R.layout.simple_list_item_1, items);

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        mInflater = (LayoutInflater)context.getSystemService(inflater);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (D) {
            Log.d(TAG, "getView position:" + position);
        }
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_list, parent, false);

            CheckBox delMe = (CheckBox)convertView.findViewById(R.id.deleteMe);
            delMe.setOnClickListener(this);

            ManageListsViewHolder holder = new ManageListsViewHolder();
            delMe.setTag(holder);
        }

        TaskList taskList = getItem(position);

        CheckBox delMe = (CheckBox)convertView.findViewById(R.id.deleteMe);

        ManageListsViewHolder holder = (ManageListsViewHolder)delMe.getTag();
        holder.mTaskList = taskList;

        String name = taskList.getName();
        TextView tv = (TextView)convertView.findViewById(R.id.name);
        tv.setText(name);

        return convertView;
    }

    @Override
    public void onClick(View view) {
        CheckBox cb = (CheckBox)view;
        ManageListsViewHolder holder = (ManageListsViewHolder)cb.getTag();
        TaskList taskList = holder.mTaskList;

        String name = taskList.getName();
        Log.d(TAG, "clicked on " + name);

        taskList.setSelected(cb.isChecked());
    }

    public static class ManageListsViewHolder {
        public TaskList mTaskList;
    }
}
