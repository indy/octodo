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
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.indy.octodo.EditTaskActivity;
import io.indy.octodo.R;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;

public class TaskItemView extends LinearLayout {

    static private final boolean D = true;
    static private final String TAG = TaskItemView.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private final Context mContext;

    private MainController mController;

    private CheckBox mIsDone;

    private TextView mContent;

    private LinearLayout mTaskRow;

    private Task mTask;

    public TaskItemView(Context context) {
        super(context);

        mContext = context;

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater)getContext().getSystemService(infService);

        li.inflate(R.layout.row_task, this, true);

        mTaskRow = (LinearLayout)findViewById(R.id.taskRow);
        mIsDone = (CheckBox)findViewById(R.id.isDone);
        mContent = (TextView)findViewById(R.id.content);

        addClickListeners();
    }

    public void setupWithTask(Task task) {

        mTask = task;

        String taskString = task.getContent();
        mContent.setText(taskString);

        int state = task.getState();
        if (state == Task.STATE_STRUCK) {
            setContentAsStruckThru();
            mIsDone.setChecked(true);
        } else {
            setContentAsNotStruckThru();
            mIsDone.setChecked(false);
        }
    }

    public void setController(MainController controller) {
        mController = controller;
    }

    private void addClickListeners() {
        mIsDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ifd("mIsDone checkbox received normal click");

                clickedIsDone();
            }
        });

        mTaskRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "taskRow received normal click");

                mIsDone.setChecked(!mIsDone.isChecked());
                clickedIsDone();
            }
        });

        mTaskRow.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ifd("taskRow received long click");

                Intent intent = new Intent(mContext, EditTaskActivity.class);
                intent.putExtra(EditTaskActivity.INTENT_EXTRA_LIST_NAME, mTask.getParentName());
                intent.putExtra(EditTaskActivity.INTENT_EXTRA_START_TIME, mTask.getStartedAt());
                mContext.startActivity(intent);

                return true;
            }
        });
    }

    private void clickedIsDone() {
        int state;

        if (mIsDone.isChecked()) {
            setContentAsStruckThru();
            state = Task.STATE_STRUCK;
        } else {
            setContentAsNotStruckThru();
            state = Task.STATE_OPEN;
        }

        mController.onTaskUpdateState(mTask, state);
    }

    private void setContentAsStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setContentAsNotStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
