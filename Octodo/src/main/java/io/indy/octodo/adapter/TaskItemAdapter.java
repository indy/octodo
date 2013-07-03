
package io.indy.octodo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.view.TaskItemView;

public class TaskItemAdapter extends ArrayAdapter<Task> {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private final Context mContext;

    private MainController mController;

    private String mTaskListName;

    public TaskItemAdapter(Context context, TaskList taskList, MainController controller) {
        super(context, android.R.layout.simple_list_item_1, taskList.getTasks());

        mController = controller;
        mContext = context;
        mTaskListName = taskList.getName();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (D) {
            Log.d(TAG, "getView position: " + position);
        }

        Task task = getItem(position);
        TaskItemView taskItemView = (TaskItemView)v;

        if (taskItemView == null) {
            taskItemView = new TaskItemView(mContext);
            taskItemView.setController(mController);
        }

        taskItemView.setupWithTask(mTaskListName, task);

        return taskItemView;
    }

}
