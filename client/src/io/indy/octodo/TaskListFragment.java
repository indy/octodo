package io.indy.octodo;

import io.indy.octodo.event.RemoveCompletedTasksEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.AnimationHelper;
import io.indy.octodo.helper.DateFormatHelper;
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
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import de.greenrobot.event.EventBus;

public final class TaskListFragment extends Fragment implements OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private List<Task> mTasks;
    private TaskItemAdapter mTaskItemAdapter;
    private TaskModelInterface mTaskModelInterface;

    private TaskList mTaskList;
    private EditText mEditText;
    private ListView mListView;
    private Button mButtonAddTask;

    private LinearLayout mSectionAddTask;

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
        if (event.getTaskListId() == mTaskList.getId()) {
            refreshTasks();
        }
    }

    public void onEvent(ToggleAddTaskFormEvent event) {

        if (event.getTaskListId() == mTaskList.getId()) {
            Animation anim;
            // toggle visibility
            if (mSectionAddTask.getVisibility() == View.GONE) {
                anim = AnimationHelper.slideDownAnimation();
                mSectionAddTask.startAnimation(anim);
                mSectionAddTask.setVisibility(View.VISIBLE);
            } else {
                anim = AnimationHelper.slideUpAnimation();
                mSectionAddTask.startAnimation(anim);
                mSectionAddTask.setVisibility(View.GONE);
            }
        } else { // not the current task list
            // set as invisible
            mSectionAddTask.setVisibility(View.GONE);
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
        mSectionAddTask = (LinearLayout) view.findViewById(R.id.sectionAddTask);

        mTasks = mTaskModelInterface.onGetTasks(mTaskList.getId());

        mTaskItemAdapter = new TaskItemAdapter(getActivity(),
                mTasks,
                mTaskModelInterface);

        // Bind the Array Adapter to the List View
        // mListView.setAdapter(mTaskItemAdapter);
        mListView.setAdapter(new SlideExpandableListAdapter(mTaskItemAdapter,
                                                            R.id.expandable_toggle_button,
                                                            R.id.expandable));


        // invoke this object's onClick method when a task is added
        mButtonAddTask.setOnClickListener(this);

        return view;
    }

    public void onClick(View v) {
        String content = mEditText.getText().toString();
        String now = DateFormatHelper.today();

        Task task = new Task.Builder().id(0).listId(mTaskList.getId())
                .content(content).state(0).startedAt(now).build();

        if (D) {
            Log.d(TAG, "adding a task");
            // this task has an id of 0, can't just add it to mTasks
            Log.d(TAG, "id: " + task.getId());
            Log.d(TAG, "content: " + task.getContent());
            Log.d(TAG, "state: " + task.getState());
            Log.d(TAG, "startedAt: " + task.getStartedAt());
            Log.d(TAG, "finishedAt: " + task.getFinishedAt());
        }

        // update db
        mTaskModelInterface.onTaskAdded(task);
        // refresh ui
        refreshTasks();

        mEditText.setText("");
    }

    // get the list of tasks from the model and display them
    private void refreshTasks() {
        List<Task> tasks = mTaskModelInterface.onGetTasks(mTaskList.getId());
        mTasks.clear();
        mTasks.addAll(tasks);
        mTaskItemAdapter.notifyDataSetChanged();
    }
}
