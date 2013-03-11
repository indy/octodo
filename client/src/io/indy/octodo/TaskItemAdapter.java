package io.indy.octodo;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskModelInterface;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

public class TaskItemAdapter extends ArrayAdapter<Task> implements
        OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private final LayoutInflater mInflater;
    private final Context mContext;

    private TaskModelInterface mTaskModelInterface;

    public TaskItemAdapter(Context context, List<Task> items,
            TaskModelInterface taskModelInterface) {
        super(context, android.R.layout.simple_list_item_1, items);

        mTaskModelInterface = taskModelInterface;

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        mInflater = (LayoutInflater) context.getSystemService(inflater);

        mContext = context;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (D) {
            Log.d(TAG, "getView position: " + position);
        }

        Task task = getItem(position);

        boolean addClickListeners = false;
        if (v == null) {
            v = mInflater.inflate(R.layout.row_task, parent, false);
            addClickListeners = true;
        }

        CheckBox isDone = (CheckBox) v.findViewById(R.id.isDone);
        if (addClickListeners) {
            isDone.setOnClickListener(this);

            ImageButton editTask;
            editTask = (ImageButton) v.findViewById(R.id.edit_task);
            editTask.setOnClickListener(createEditTaskListener(task));

            ImageButton deleteTask;
            deleteTask = (ImageButton) v.findViewById(R.id.delete_task);
            deleteTask.setOnClickListener(createDeleteTaskListener(task));

        }
        Integer taskId = Integer.valueOf(task.getId());
        isDone.setTag(taskId);

        TextView tv = (TextView) v.findViewById(R.id.content);
        String taskString = task.getContent();
        tv.setText(taskString);

        int state = task.getState();
        if (state == Task.STATE_STRUCK) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            isDone.setChecked(true);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            isDone.setChecked(false);
        }

        return v;
    }

    @Override
    public void onClick(View view) {
        CheckBox cb = (CheckBox) view;
        int id = (Integer) view.getTag();

        View parent = (View) view.getParent();
        TextView tv = (TextView) parent.findViewById(R.id.content);

        if (cb.isChecked()) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            mTaskModelInterface.onTaskUpdateState(id, Task.STATE_STRUCK);
            Log.d(TAG, "true isChecked on id: " + id);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            mTaskModelInterface.onTaskUpdateState(id, Task.STATE_OPEN);
            Log.d(TAG, "false isChecked on id: " + id);
        }
    }

    private View.OnClickListener createEditTaskListener(Task task) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked editTask button");
            }
        };

        return listener;
    }

    private View.OnClickListener createDeleteTaskListener(final Task task) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked deleteTask button");

                String title = "Delete Task";
                String message = "Permanently delete \"" + task.getContent() + "\"?";
                String button1String = "Delete";
                String button2String = "Cancel";

                AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
                ad.setTitle(title);
                ad.setMessage(message);
                ad.setPositiveButton(button1String,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                Log.d(TAG, "eaten by grue");
                            }
                        });

                ad.setNegativeButton(button2String,
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
        };

        return listener;
    }

}
