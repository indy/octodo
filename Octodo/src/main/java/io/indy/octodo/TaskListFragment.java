/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.indy.octodo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.adapter.TaskItemAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.LoadedTaskListsEvent;
import io.indy.octodo.event.MoveTaskEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.helper.DateFormatHelper;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public final class TaskListFragment extends Fragment {

    static private final boolean D = true;
    static private final String TAG = TaskListFragment.class.getSimpleName();
    void ifd(final String message) { if(D) Log.d(TAG, "[" + System.identityHashCode(this) + "] " + message); }

    private TaskItemAdapter mTaskItemAdapter;

    private MainController mController;

    private TaskList mTaskList;

    private EditText mEditText;

    private ListView mListView;

    private Context mContext;

    public static TaskListFragment newInstance(String taskListName) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setTaskList(taskListName);
        Log.d("TaskListFragment", "newInstance: " + taskListName);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ifd("onAttach");
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ifd("onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ifd("onCreateView");

        // Create, or inflate the Fragment's UI, and return it.
        // If this Fragment has no UI then return null.
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

        mEditText = (EditText)view.findViewById(R.id.editTextTask);
        mListView = (ListView)view.findViewById(R.id.listViewTasks);

        setKeyboardListener(mEditText);

        if (mTaskList == null) {
            String taskListName = savedInstanceState.getString("taskListName");
            ifd("mTaskList is null for " + taskListName + " - do some re-initialisation with OctodoModel?");
            mTaskList = new TaskList(taskListName);
            // TODO: would updateLocalTaskList fail here?
        } //else {
            // already have a mTaskList from setInstance
//            updateLocalTaskList();
       // }



        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ifd("onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        ifd("onStart");
        // Apply any required UI change now that the Activity is visible.

        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.
        ifd("registering: " + System.identityHashCode(this));
        EventBus.getDefault().register(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        ifd("onResume");

        mController = ((MainActivity)mContext).getController();

        updateLocalTaskList();

        mTaskItemAdapter = new TaskItemAdapter(getActivity(), mTaskList, mController);
        mListView.setAdapter(mTaskItemAdapter);
    }

    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
        ifd("onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate and
        // onRestoreInstanceState if the process is
        // killed and restarted by the run time.
        super.onSaveInstanceState(savedInstanceState);
        ifd("onSaveInstanceState");
        // save the taskList
        savedInstanceState.putString("taskListName", mTaskList.getName());
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        // Suspend remaining UI updates, threads, or processing
        // that aren't required when the Activity isn't visible.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onStop();
        ifd("onStop");

        ifd("unregistering: " + System.identityHashCode(this));
        EventBus.getDefault().unregister(this);
    }

    // Sometimes called at the end of the full lifetime.
    @Override
    public void onDestroyView() {
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroyView();
        ifd("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ifd("onDestroy");
    }


    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(LoadedTaskListsEvent event) {
        ifd("received LoadedTaskListsEvent");
        refreshUI();
    }

    // common event fired whenever a task is modified and it's parent tasklist
    // UI needs to be updated
    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(RefreshTaskListEvent event) {
        ifd("received RefreshTaskListEvent");
        if (isEventRelevant(event.getTaskListName())) {
            ifd("valid RefreshTaskListEvent received in TaskListFragment");
            refreshUI();
            mEditText.setText("");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(MoveTaskEvent event) {
        if (isEventRelevant(event.getOldTaskList()) || isEventRelevant(event.getNewTaskList())) {
            refreshUI();
        }
    }

    public void setTaskList(String taskListName) {
        //mTaskList = taskList;

        // create a local copy of the taskList
        // this.mTaskList will provide data to this fragment's ui

        //mTaskList = new TaskList(taskList.getId(), taskList.getName());
        mTaskList = new TaskList(taskListName);

        //List<Task> localTasks = mTaskList.getTasks();
        //localTasks.clear();
        //localTasks.addAll(taskList.getTasks());

        ifd("mTaskList set to " + taskListName);
    }

    private void setKeyboardListener(EditText editText) {

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66 && event.getAction() == 1) {
                    addTask();
                }
                return false;
            }
        });
    }

    private void addTask() {
        String content = mEditText.getText().toString().trim();
        if (content.length() == 0) {
            // don't add empty strings
            return;
        }

        String now = DateFormatHelper.today();

        Task task = new Task.Builder()
                .content(content)
                .state(0)
                .startedAt(now)
                .build();

        if (D) {
            ifd("adding a task");
            // this task has an id of 0, can't just add it to mTasks
            ifd("content: " + task.getContent());
            ifd("state: " + task.getState());
            ifd("startedAt: " + task.getStartedAt());
            ifd("finishedAt: " + task.getFinishedAt());
        }

        // update db
        mController.onTaskAdd(task, mTaskList.getName());
    }

    private void updateLocalTaskList() {
        ifd("updateLocalTaskList (mController=" + System.identityHashCode(mController) + ")");

        TaskList tasklist = mController.onGetTaskList(mTaskList.getName());
        List<Task> tasks = tasklist.getTasks();

        List<Task> localTasks = mTaskList.getTasks();
        localTasks.clear();
        localTasks.addAll(tasks);

        if(D) {
            for(Task t: localTasks) {
                ifd("taskContent: " + t.getContent());
            }
        }
    }

    // get the list of tasks from the model and display them
    private void refreshUI() {
        updateLocalTaskList();
        mTaskItemAdapter.notifyDataSetChanged();
    }

    // TODO: remove this
    private boolean isEventRelevant(TaskList taskList) {
        return mTaskList.getName().equals(taskList.getName());
    }

    private boolean isEventRelevant(String taskListName) {
        return mTaskList.getName().equals(taskListName);
    }
}
