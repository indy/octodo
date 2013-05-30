
package io.indy.octodo;

import io.indy.octodo.adapter.TaskItemAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.helper.AnimationHelper;
import io.indy.octodo.helper.DateFormatHelper;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
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

    private SlideExpandableListAdapter mSlideAdapter;

    private MainController mController;

    private TaskList mTaskList;

    private EditText mEditText;

    private ListView mListView;

    private Button mButtonAddTask;

    private LinearLayout mSectionAddTask;

    private Context mContext;

    public static TaskListFragment newInstance(TaskList taskList) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setTaskList(taskList);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (D) {
            Log.d(TAG, "onAttach");
        }

        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (D)
            Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (D) {
            Log.d(TAG, "onCreateView");
        }

        // Create, or inflate the Fragment's UI, and return it.
        // If this Fragment has no UI then return null.
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

        mEditText = (EditText)view.findViewById(R.id.editTextTask);
        mListView = (ListView)view.findViewById(R.id.listViewTasks);
        mButtonAddTask = (Button)view.findViewById(R.id.buttonAddTask);
        mSectionAddTask = (LinearLayout)view.findViewById(R.id.sectionAddTask);

        setKeyboardVisibility(mEditText);

        mController = ((MainActivity)mContext).getController();
        if (mTaskList == null) {
            int taskListId = savedInstanceState.getInt("taskListId");
            // get TaskList with the given Id from the activity
            mTaskList = ((MainActivity)mContext).getTaskList(taskListId);
        }

        mTasks = mController.onGetTasks(mTaskList.getId());

        mTaskItemAdapter = new TaskItemAdapter(getActivity(), mTasks, mController);

        mSlideAdapter = new SlideExpandableListAdapter(mTaskItemAdapter, R.id.expandable_trigger,
                R.id.expandable);

        // Bind the Array Adapter to the List View
        mListView.setAdapter(mSlideAdapter);

        // invoke this object's onClick method when a task is added
        mButtonAddTask.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) {
            Log.d(TAG, "onActivityCreated");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) {
            Log.d(TAG, "onStart");
        }
        // Apply any required UI change now that the Activity is visible.
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) {
            Log.d(TAG, "onResume");
        }
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.
    }

    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
        if (D) {
            Log.d(TAG, "onPause");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.
        super.onSaveInstanceState(savedInstanceState);
        if (D) {
            Log.d(TAG, "onSaveInstanceState");
        }

        // save the taskList
        savedInstanceState.putInt("taskListId", mTaskList.getId());
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        // Suspend remaining UI updates, threads, or processing
        // that aren't required when the Activity isn't visible.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onStop();
        if (D) {
            Log.d(TAG, "onStop");
        }
    }

    // Sometimes called at the end of the full lifetime.
    @Override
    public void onDestroyView() {
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroyView();
        if (D) {
            Log.d(TAG, "onDestroyView");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // common event fired whenever a task is modified and it's parent tasklist
    // UI needs to be updated
    public void onEvent(RefreshTaskListEvent event) {
        if (isEventRelevant(event.getTaskListId())) {
            refreshTasks();
            mEditText.setText("");
        }
    }

    public void onEvent(MoveTaskEvent event) {
        if (isEventRelevant(event.getOldTaskListId()) || isEventRelevant(event.getNewTaskListId())) {
            refreshTasks();
        }
    }

    public void onEvent(ToggleAddTaskFormEvent event) {
        if (isEventRelevant(event.getTaskListId())) {

            int h = mSectionAddTask.getMeasuredHeight();
            float fakeheight = 96.0f;
            float height = h == 0 ? fakeheight : (float)h;

            Animation anim;
            if (mSectionAddTask.getVisibility() == View.GONE) {
                // show the 'add task' field
                anim = AnimationHelper.slideDownAnimation();
                mSectionAddTask.startAnimation(anim);
                mSectionAddTask.setVisibility(View.VISIBLE);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEditText.requestFocus();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                Animation listAnim = AnimationHelper.slideDownAnimation(height);
                mListView.startAnimation(listAnim);

            } else {

                anim = AnimationHelper.slideUpAnimation();
                mSectionAddTask.startAnimation(anim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSectionAddTask.setVisibility(View.GONE);
                        // prevents flicker when mSectionAddTask is hidden
                        mListView.clearAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                Animation listAnim = AnimationHelper.slideUpAnimation(height);
                mListView.startAnimation(listAnim);
            }
        } else { // not the current task list
            // set as invisible
            mSectionAddTask.setVisibility(View.GONE);
        }
    }

    public void setTaskList(TaskList taskList) {
        mTaskList = taskList;
        if (D) {
            Log.d(TAG, "mTaskList set to " + taskList);
        }
    }

    private void setKeyboardVisibility(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                InputMethodManager imm = (InputMethodManager)mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (hasFocus) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        String content = mEditText.getText().toString().trim();
        if (content.length() == 0) {
            // don't add empty strings
            return;
        }

        String now = DateFormatHelper.today();

        Task task = new Task.Builder().id(0).listId(mTaskList.getId()).content(content).state(0)
                .startedAt(now).build();

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
        mController.onTaskAdded(task);
    }

    // get the list of tasks from the model and display them
    private void refreshTasks() {

        mSlideAdapter.collapseLastOpen();

        List<Task> tasks = mController.onGetTasks(mTaskList.getId());
        mTasks.clear();
        mTasks.addAll(tasks);
        mTaskItemAdapter.notifyDataSetChanged();
    }

    private boolean isEventRelevant(int taskListId) {
        return mTaskList.getId() == taskListId;
    }
}