package io.indy.octodo.view;

import io.indy.octodo.R;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.model.TaskModelInterface;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskItemView extends LinearLayout {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private final Context mContext;
    private TaskModelInterface mModel;

    private CheckBox mIsDone;
    private ImageButton mEditTask;
    private ImageButton mDeleteTask;
    private ImageButton mMoveTask;
    private TextView mContent;

    private Task mTask;

    public TaskItemView(Context context) {
        super(context);

        mContext = context;

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService(infService);

        li.inflate(R.layout.row_task, this, true);

        mIsDone = (CheckBox) findViewById(R.id.isDone);
        mEditTask = (ImageButton) findViewById(R.id.edit_task);
        mDeleteTask = (ImageButton) findViewById(R.id.delete_task);
        mMoveTask = (ImageButton) findViewById(R.id.move_task);
        mContent = (TextView) findViewById(R.id.content);

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

    public void setTaskModelInterface(TaskModelInterface taskModelInterface) {
        mModel = taskModelInterface;
    }

    private void addClickListeners() {
        mIsDone.setOnClickListener(mOnClickListener);
        mEditTask.setOnClickListener(mOnClickListener);
        mMoveTask.setOnClickListener(mOnClickListener);
        mDeleteTask.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.isDone:
                clickedIsDone(v);
                break;
            case R.id.edit_task:
                clickedEditTask(v);
                break;
            case R.id.delete_task:
                clickedDeleteTask(v);
                break;
            case R.id.move_task:
                clickedMoveTask(v);
                break;
            }
        }
    };

    private void clickedIsDone(View view) {
        CheckBox cb = (CheckBox) view;
        int state;

        if (cb.isChecked()) {
            setContentAsStruckThru();
            state = Task.STATE_STRUCK;
        } else {
            setContentAsNotStruckThru();
            state = Task.STATE_OPEN;
        }
        
        mModel.onTaskUpdateState(mTask, state);
    };

    private void clickedEditTask(View view) {
        Log.d(TAG, "clicked editTask button");
    }

    private void clickedDeleteTask(View view) {

        String title = "Delete Task";
        String message = "Permanently delete task?";
        String positiveString = "Delete";
        String negativeString = "Cancel";

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(positiveString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the delete button");
                        }
                        // close the drawer
                        mModel.onTaskDelete(mTask);
                    }
                });

        ad.setNegativeButton(negativeString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the cancel button");
                        }
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (D) {
                    Log.d(TAG, "pressed cancel");
                }
            }
        });

        ad.show();

    }

    private void clickedMoveTask(View view) {
        Log.d(TAG, "clicked moveTask button");

        String title = "Move to another list";
        String message = "Permanently delete task?";
        String positiveString = "Move task";
        String negativeString = "Cancel";

        final List<TaskList> taskLists = mModel.onGetTaskLists();
        final int taskListSize = taskLists.size();
        final CharSequence[] listNames = new CharSequence[taskListSize];

        TaskList taskList;
        int currentTaskIndex = 0;
        
        for(int i=0;i<taskListSize;i++) {
            taskList = taskLists.get(i);

            listNames[i] = taskList.getName();
            if(taskList.getId() == mTask.getListId()) {
                currentTaskIndex = i;
            }
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setTitle(title);
        ad.setSingleChoiceItems(listNames, currentTaskIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(D){
                        Log.d(TAG, "made a selection " + which);
                    }
                }
            });
        ad.setPositiveButton(positiveString,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int s = ((AlertDialog) dialog).getListView()
                                .getCheckedItemPosition();
                        if(D){
                            Log.d(TAG, "checked " + s);
                            Log.d(TAG, "which is " + which);
                        }

                        // get the listId of the selected item
                        Log.d(TAG, "chosen " + taskLists.get(s).getName());
                        Log.d(TAG, "chosen " + taskLists.get(s).getId());

                        moveTask(taskLists.get(s).getId());

                    }
                });


        ad.setNegativeButton(negativeString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (D) {
                            Log.d(TAG, "pressed the cancel button");
                        }
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (D) {
                    Log.d(TAG, "pressed cancel");
                }
            }
        });

        ad.show();

    }

    // move mTask to the list with id: taskListId
    private void moveTask(int taskListId) {
        mModel.onTaskMove(mTask, taskListId);
    }

    private void setContentAsStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags()
                | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setContentAsNotStruckThru() {
        mContent.setPaintFlags(mContent.getPaintFlags()
                & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
