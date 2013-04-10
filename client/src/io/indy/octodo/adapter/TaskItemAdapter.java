package io.indy.octodo.adapter;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskModelInterface;
import io.indy.octodo.view.TaskItemView;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TaskItemAdapter extends ArrayAdapter<Task> {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private final Context mContext;
    private TaskModelInterface mTaskModelInterface;

    public TaskItemAdapter(Context context, List<Task> items,
            TaskModelInterface taskModelInterface) {
        super(context, android.R.layout.simple_list_item_1, items);

        mTaskModelInterface = taskModelInterface;
        mContext = context;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (D) {
            Log.d(TAG, "getView position: " + position);
        }

        Task task = getItem(position);
        TaskItemView taskItemView = (TaskItemView) v;

        if (taskItemView == null) {
            taskItemView = new TaskItemView(mContext);
            taskItemView.setTaskModelInterface(mTaskModelInterface);
        }

        taskItemView.setupWithTask(task);

        return taskItemView;
    }

}
