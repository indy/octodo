package io.indy.octodo;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

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

public final class TaskListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    // implemented by the parent activity
    public interface TaskModelInterface { 
        public void onNewTaskAdded(Task newTask);
        public List<Task> getTasks(int taskListId);
    }

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
            mTaskModelInterface = (TaskModelInterface)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnNewItemAddedListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D)
            Log.d(TAG, "onCreate");
    }

    public void setTaskList(TaskList taskList) {
        mTaskList = taskList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Create, or inflate the Fragment's UI, and return it.
        // If this Fragment has no UI then return null.
        View view = inflater.inflate(R.layout.fragment_tasklist, container,
                false);
        
        mEditText = (EditText) view.findViewById(R.id.editTextTask);
        mListView = (ListView) view.findViewById(R.id.listViewTasks);
        mButtonAddTask = (Button) view.findViewById(R.id.buttonAddTask);

        mTasks = mTaskModelInterface.getTasks(mTaskList.getId());
        mTaskItemAdapter = new TaskItemAdapter(getActivity(), mTasks);
        
        
        // Bind the Array Adapter to the List View
        mListView.setAdapter(mTaskItemAdapter);

        mButtonAddTask.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {                
                String content = mEditText.getText().toString();

                Task task = new Task(0, mTaskList.getId(), content, 0);
                addTask(task);

                mEditText.setText("");
            }
        });        

        return view;
    }

    private void addTask(Task task) {
        mTaskModelInterface.onNewTaskAdded(task);
        mTasks.add(0, task);
        mTaskItemAdapter.notifyDataSetChanged();

    }
}
