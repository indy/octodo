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
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

import io.indy.octodo.R;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class TaskItemView extends LinearLayout {

    static private final boolean D = true;
    static private final String TAG = TaskItemView.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private final Context mContext;

    private ImageView mOverflowButton;

    private MainController mController;

    private CheckBox mIsDone;

    private TextView mContent;

    private TextView mTimingTextView;

    private LinearLayout mTaskRow;

    private RelativeLayout mRelativeLayout;

    private Task mTask;

    private Fragment mFragment;

    public TaskItemView(Context context) {
        super(context);

        mContext = context;

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService(infService);

        li.inflate(R.layout.row_task, this, true);

        mTaskRow = (LinearLayout) findViewById(R.id.taskRow);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mIsDone = (CheckBox) findViewById(R.id.isDone);
        mContent = (TextView) findViewById(R.id.content);
        mTimingTextView = (TextView) findViewById(R.id.timingTextView);
        mOverflowButton = (ImageView) findViewById(R.id.overflowButton);

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

        setTimingInformation();
    }

    private String getTimingText(Task task) {
        int days = task.ageInDays();

        String text;

        if(days > 14) {
            text = "" + (days / 7) + " weeks old";
        } else {
            switch(days) {
                case 0 :
                    text = "created today";
                    break;
                case 1 :
                    text = "1 day old";
                    break;
                default:
                    text = "" + days + " days old";
            }
        }

        return text;
    }

    private int getTimingColor(Task task) {
        return (task.ageInDays() < 14) ? Color.rgb(107, 195, 69) : Color.rgb(194, 70, 68);
    }

    private void setTimingInformation() {

        SpannableString timingSpan = new SpannableString(getTimingText(mTask));
        ForegroundColorSpan col = new ForegroundColorSpan(getTimingColor(mTask));
        timingSpan.setSpan(col, 0, timingSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTimingTextView.setText(timingSpan);
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

        mRelativeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "relativeLayout received normal click");

                mIsDone.setChecked(!mIsDone.isChecked());
                clickedIsDone();
            }
        });

        mOverflowButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.setOnMenuItemClickListener(popupListener);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_task, popup.getMenu());
                popup.show();
            }
        });
    }

    private PopupMenu.OnMenuItemClickListener popupListener = new PopupMenu.OnMenuItemClickListener() {

        // https://developer.android.com/guide/topics/ui/menus.html#context-menu
        // https://developer.android.com/guide/topics/ui/menus.html
        // https://developer.android.com/reference/android/widget/PopupMenu.html

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.menu_task_edit:
                    clickedEditTask();
                    return true;
                case R.id.menu_task_move:
                    clickedMoveTask();
                    return true;
                case R.id.menu_task_delete:
                    clickedDeleteTask();
                    return true;
                default:
                    return false;
            }
        }
    };


    public void setFragment(Fragment fragment) {
        mFragment = fragment;
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


    private void clickedEditTask() {
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

    private void clickedDeleteTask() {

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

    private void clickedMoveTask() {
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
            if (taskList.getName().equals(mTask.getParentName())) {
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
                        }

                        String destinationTaskList = taskLists.get(s).getName();
                        mController.onTaskMove(mTask, destinationTaskList);

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

    private void setContentAsStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setContentAsNotStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
