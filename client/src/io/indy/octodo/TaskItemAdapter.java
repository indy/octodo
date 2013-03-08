package io.indy.octodo;

import io.indy.octodo.helper.DateFormatHelper;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskModelInterface;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class TaskItemAdapter extends ArrayAdapter<Task> implements
        OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private final LayoutInflater mInflater;

    private TaskModelInterface mTaskModelInterface;

    public TaskItemAdapter(Context context, List<Task> items,
            TaskModelInterface taskModelInterface) {
        super(context, android.R.layout.simple_list_item_1, items);

        mTaskModelInterface = taskModelInterface;

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        mInflater = (LayoutInflater) context.getSystemService(inflater);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (D) {
            Log.d(TAG, "getView position: " + position);
        }

        Task task = getItem(position);

        boolean addClickListener = false;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_task, parent, false);
            addClickListener = true;
        }

        CheckBox isDone = (CheckBox) convertView.findViewById(R.id.isDone);
        if (addClickListener) {
            isDone.setOnClickListener(this);
        }
        Integer taskId = Integer.valueOf(task.getId());
        isDone.setTag(taskId);

        TextView tv = (TextView) convertView.findViewById(R.id.content);
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

        return convertView;
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

}
