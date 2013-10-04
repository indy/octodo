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

package io.indy.octodo.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import io.indy.octodo.R;
import io.indy.octodo.event.ToggledListSelectionEvent;
import io.indy.octodo.model.TaskList;

public class ManageListItemView extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private TaskList mTaskList;

    public ManageListItemView(Context context) {
        super(context);

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService(infService);

        li.inflate(R.layout.row_list, this, true);
    }

    public void setup(TaskList taskList) {
        mTaskList = taskList;

        TextView tv = (TextView) findViewById(R.id.name);
        tv.setText(mTaskList.getName());

        CheckBox cb = (CheckBox) findViewById(R.id.deleteMe);
        cb.setChecked(mTaskList.isSelected());

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedDeleteMe(v);
            }
        });
    }

    private void clickedDeleteMe(View view) {
        if (D) {
            Log.d(TAG, "clickedDeleteMe");
        }

        CheckBox cb = (CheckBox) view;
        mTaskList.setSelected(cb.isChecked());
        postToggleListSelectionEvent();
    }

    private void postToggleListSelectionEvent() {
        EventBus.getDefault().post(new ToggledListSelectionEvent());
    }
}
