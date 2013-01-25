package io.indy.octodo;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskModelInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Paint;
import android.text.format.DateUtils;
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

    private TaskModelInterface mTaskModelInterface;

    public TaskItemAdapter(Context context, List<Task> items,
            TaskModelInterface taskModelInterface) {
        super(context, android.R.layout.simple_list_item_1, items);

        mTaskModelInterface = taskModelInterface;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (D) {
            Log.d(TAG, "getView position: " + position);
        }

        Task task = getItem(position);

        if (convertView == null) {

            if (D) {
                Log.d(TAG, "convertView is null");
            }

            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(inflater);
            convertView = layoutInflater.inflate(R.layout.row_task,
                    parent,
                    false);

            CheckBox cb = (CheckBox) convertView.findViewById(R.id.isDone);
            cb.setOnClickListener(this);
        }

        String taskString = task.getContent();
        int state = task.getState();

        if (D) {
            Log.d(TAG, taskString + " state:" + state);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.content);
        tv.setText(taskString);

        String startedAtString = task.getStartedAt();
        TextView ageTv = (TextView) convertView.findViewById(R.id.age);
        String timeSpan = formatTimeSpan(startedAtString);
        ageTv.setText(timeSpan);


        Integer taskId = Integer.valueOf(task.getId());
        CheckBox isDone = (CheckBox) convertView.findViewById(R.id.isDone);
        isDone.setTag(taskId);

        if (state == Task.STATE_STRUCK) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            isDone.setChecked(true);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            isDone.setChecked(false);
        }

        if (D) {
            Log.d(TAG, "---------------");
        }

        return convertView;
    }

    public String formatTimeSpan(String timeToFormat) {

        String timeSpan = "";

        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }
            if (date != null) {
                long time = date.getTime();
                timeSpan = (String) DateUtils.getRelativeTimeSpanString(time);
                // Log.d(TAG, "timeSpan is " + timeSpan);
            }
        }
        return timeSpan;
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
