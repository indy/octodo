package io.indy.octodo;

import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

    private TaskModelInterface mTaskModelInterface;

    private TaskList mTaskList;
    private EditText mEditText;
    private ListView mListView;
    private Button mButtonAddTask;

    // private static final String KEY_CONTENT = "TestFragment:Content";

    public static TaskListFragment newInstance(TaskList taskList) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setTaskList(taskList);
        return fragment;
    }

    // private String mContent = "???";

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

        /*
        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
        */

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

        // Create the Array List of to do items
        final ArrayList<String> todoItems = new ArrayList<String>();
        List<Task> tasks = mTaskModelInterface.getTasks(mTaskList.getId());
        for(Task t : tasks) {
            Log.d(TAG,t.getContent());
            todoItems.add(t.getContent());
        }

        // Create the Array Adapter to bind the array to the List View final
        final ArrayAdapter<String> aa = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, todoItems);
        // Bind the Array Adapter to the List View
        mListView.setAdapter(aa);

        mButtonAddTask.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {                
                String content = mEditText.getText().toString();

                Task task = new Task(0, mTaskList.getId(), content, 0);
                mTaskModelInterface.onNewTaskAdded(task);

                todoItems.add(0, content);
                aa.notifyDataSetChanged();
                mEditText.setText("");
            }
        });        
        
        Log.d(TAG, "registering on key listener");
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "event is " + event.getAction());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "keycode is " + keyCode);
                    if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                            || (keyCode == KeyEvent.KEYCODE_ENTER)) {

                        String content = mEditText.getText().toString();

                        Task task = new Task(0, mTaskList.getId(), content, 0);
                        mTaskModelInterface.onNewTaskAdded(task);

                        todoItems.add(0, content);
                        aa.notifyDataSetChanged();
                        mEditText.setText("");

                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }
    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
    */
}
