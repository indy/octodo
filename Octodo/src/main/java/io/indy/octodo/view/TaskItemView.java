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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.indy.octodo.EditTaskActivity;
import io.indy.octodo.R;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class TaskItemView extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private final Context mContext;

    private MainController mController;

    private CheckBox mIsDone;

    private ImageButton mEditTask;

    private ImageButton mDeleteTask;

    private ImageButton mMoveTask;

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
/*
        mEditTask = (ImageButton)findViewById(R.id.edit_task);
        mDeleteTask = (ImageButton)findViewById(R.id.delete_task);
        mMoveTask = (ImageButton)findViewById(R.id.move_task);
*/
        addClickListeners();
    }

    public void setupWithTask(Task task) {

        mTask = task;

        // Integer taskId = Integer.valueOf(mTaskId);
        // mIsDone.setTag(taskId);

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
        mIsDone.setOnClickListener(mOnClickListener);
        //mEditTask.setOnClickListener(mOnClickListener);
        //mMoveTask.setOnClickListener(mOnClickListener);
        //mDeleteTask.setOnClickListener(mOnClickListener);


        mTaskRow.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "taskRow received long click");
                Intent intent = new Intent(mContext, EditTaskActivity.class);
                mContext.startActivity(intent);
                return true;
            }
        });

        mTaskRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "taskRow received normal click");

                mIsDone.setChecked(!mIsDone.isChecked());
                clickedIsDone(mIsDone);
            }
        });
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.isDone:
                    clickedIsDone(v);
                    break;
                /*
                case R.id.edit_task:
                    clickedEditTask(v);
                    break;
                case R.id.delete_task:
                    clickedDeleteTask(v);
                    break;
                case R.id.move_task:
                    clickedMoveTask(v);
                    break;
                */
            }
        }
    };

    private void clickedIsDone(View view) {
        CheckBox cb = (CheckBox)view;
        int state;

        if (cb.isChecked()) {
            setContentAsStruckThru();
            state = Task.STATE_STRUCK;
        } else {
            setContentAsNotStruckThru();
            state = Task.STATE_OPEN;
        }

        mController.onTaskUpdateState(mTask, state);
    }

    private void clickedEditTask(View view) {
        if (D) {
            Log.d(TAG, "clickedEditTask");
        }

        final String content = mTask.getContent();

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setTitle(mContext.getString(R.string.edit_task_title));

        final EditText input = new EditText(mContext);

        input.setText(content);
        input.selectAll();

        ad.setView(input);
        ad.setPositiveButton(mContext.getString(R.string.edit_task_positive),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the positive button");
                        }
                        final String newContent = input.getText().toString().trim();
                        hideSoftKeyboard(input);
                        mController.onTaskUpdateContent(mTask, newContent);
                    }
                });
        ad.setNegativeButton(mContext.getString(R.string.dialog_generic_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the cancel button");
                        }
                    }
                });

        ad.show();

    }

    private void hideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager)mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void clickedDeleteTask(View view) {

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setTitle(mContext.getString(R.string.delete_task_title));
        ad.setMessage(mContext.getString(R.string.delete_task_message));
        ad.setPositiveButton(mContext.getString(R.string.delete_task_positive),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the delete button");
                        }
                        // close the drawer
                        mController.onTaskDelete(mTask);
                    }
                });

        ad.setNegativeButton(mContext.getString(R.string.dialog_generic_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the cancel button");
                        }
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (D) {
                    Log.d(TAG, "pressed cancel");
                }
            }
        });

        ad.show();
    }

    private void clickedMoveTask(View view) {
        if (D) {
            Log.d(TAG, "clicked moveTask button");
        }

        final List<TaskList> taskLists = mController.onGetTaskLists();
        final int taskListSize = taskLists.size();
        final CharSequence[] listNames = new CharSequence[taskListSize];

        TaskList taskList;
        int currentTaskIndex = 0;

        for (int i = 0; i < taskListSize; i++) {
            taskList = taskLists.get(i);

            listNames[i] = taskList.getName();
            if (taskList.getId() == mTask.getListId()) {
                currentTaskIndex = i;
            }
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setTitle(mContext.getString(R.string.move_task_title));
        ad.setSingleChoiceItems(listNames, currentTaskIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (D) {
                    Log.d(TAG, "made a selection " + which);
                }
            }
        });
        ad.setPositiveButton(mContext.getString(R.string.move_task_positive),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int s = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        if (D) {
                            Log.d(TAG, "checked " + s);
                            Log.d(TAG, "which is " + which);
                            // get the listId of the selected item
                            Log.d(TAG, "chosen " + taskLists.get(s).getName());
                            Log.d(TAG, "chosen " + taskLists.get(s).getId());
                        }

                        String destinationTaskList = taskLists.get(s).getName();
                        moveTask(destinationTaskList);
                    }
                });

        ad.setNegativeButton(mContext.getString(R.string.dialog_generic_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the cancel button");
                        }
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (D) {
                    Log.d(TAG, "pressed cancel");
                }
            }
        });

        ad.show();

    }

    private void moveTask(String destinationTaskList) {
        mController.onTaskMove(mTask, destinationTaskList);
    }

    private void setContentAsStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setContentAsNotStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
