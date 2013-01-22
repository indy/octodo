package io.indy.octodo;

import io.indy.octodo.event.RemoveCompletedTasksEvent;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.model.TaskModelInterface;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.greenrobot.event.EventBus;

public final class TaskListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private List<Task> mTasks;
    private TaskItemAdapter mTaskItemAdapter;
    private TaskModelInterface mTaskModelInterface;

    private TaskList mTaskList;
    private EditText mEditText;
    private ListView mListView;
    private Button mButtonAddTask;

    public static TaskListFragment newInstance(TaskList taskList) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setTaskList(taskList);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (D)
            Log.d(TAG, "onAttach");

        try {
            mTaskModelInterface = (TaskModelInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnNewItemAddedListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (D)
            Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(RemoveCompletedTasksEvent event) {
        int taskListId = event.getTaskListId();
        if(taskListId == mTaskList.getId()) {

            List<Task> tasks = mTaskModelInterface.onGetTasks(mTaskList.getId());
            mTasks.clear();
            mTasks.addAll(tasks);
            mTaskItemAdapter.notifyDataSetChanged();

            Log.d(TAG, "notifyDataSetChanged");

        }
    }

    public void setTaskList(TaskList taskList) {
        mTaskList = taskList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // Create, or inflate the Fragment's UI, and return it.
        // If this Fragment has no UI then return null.
        View view = inflater.inflate(R.layout.fragment_tasklist,
                container,
                false);

        mEditText = (EditText) view.findViewById(R.id.editTextTask);
        mListView = (ListView) view.findViewById(R.id.listViewTasks);
        mButtonAddTask = (Button) view.findViewById(R.id.buttonAddTask);

        mTasks = mTaskModelInterface.onGetTasks(mTaskList.getId());

        mTaskItemAdapter = new TaskItemAdapter(getActivity(), mTasks, mTaskModelInterface);

        // Bind the Array Adapter to the List View
        mListView.setAdapter(mTaskItemAdapter);

        mButtonAddTask.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String content = mEditText.getText().toString();

                // Task task = new Task(0, mTaskList.getId(), content, 0);

                Task task = new Task.Builder().id(0).listId(mTaskList.getId())
                        .content(content).state(0).build();

                addTask(task);

                mEditText.setText("");
            }
        });

        return view;
    }

    private void addTask(Task task) {
        mTaskModelInterface.onTaskAdded(task);
        mTasks.add(mTasks.size(), task);
        mTaskItemAdapter.notifyDataSetChanged();
    }
}
