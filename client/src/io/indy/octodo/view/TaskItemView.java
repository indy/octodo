package io.indy.octodo.view;

import io.indy.octodo.R;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskModelInterface;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.AttributeSet;
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
    private TaskModelInterface mModelInterface;

    private CheckBox mIsDone;
    private ImageButton mEditTask;
    private ImageButton mDeleteTask;
    private ImageButton mMoveTask;
    private TextView mContent;

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
    }

    public TaskItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setupWithTask(Task task) {
        Integer taskId = Integer.valueOf(task.getId());
        mIsDone.setTag(taskId);

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
        mModelInterface = taskModelInterface;
    }

    public void addClickListeners() {
        mIsDone.setOnClickListener(createIsDoneTaskListener());
        mEditTask.setOnClickListener(createEditTaskListener());
        mDeleteTask.setOnClickListener(createDeleteTaskListener());
        mMoveTask.setOnClickListener(createMoveTaskListener());
    }

    private View.OnClickListener createIsDoneTaskListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) view;
                int id = (Integer) view.getTag();

                if (cb.isChecked()) {
                    setContentAsStruckThru();
                    mModelInterface.onTaskUpdateState(id, Task.STATE_STRUCK);
                } else {
                    setContentAsNotStruckThru();
                    mModelInterface.onTaskUpdateState(id, Task.STATE_OPEN);
                }
            }
        };
    }

    private View.OnClickListener createEditTaskListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked editTask button");
            }
        };
    }

    private View.OnClickListener createDeleteTaskListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        };
    }

    private View.OnClickListener createMoveTaskListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked move task button");
            }
        };
    }

    private void showDeleteDialog() {
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
                        Log.d(TAG, "eaten by grue");
                    }
                });

        ad.setNegativeButton(negativeString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Log.d(TAG, "pressed the cancel button");
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "pressed cancel");
            }
        });

        ad.show();

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
