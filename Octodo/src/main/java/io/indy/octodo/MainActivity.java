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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.adapter.TaskListPagerAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.HaveCurrentTaskListEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.model.TaskList;

public class MainActivity extends DriveBaseActivity {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private TaskListPagerAdapter mAdapter;

    private ViewPager mPager;

    private PageIndicator mIndicator;

    private MainController mController;

    private List<String> mTaskListNames;

    private boolean mShowTrashIcon;

    private int mTrashItemId;

    // TODO: check why this is used now
    public void refreshTaskListsUI() {
        if (D) {
            Log.d(TAG, "refreshTaskListsUI");
        }

        List<TaskList> lists = mController.onGetTaskLists();

        // re-create the list of tasklist names and notify mAdapter of change
        if (lists != null) {
            mTaskListNames.clear();
            for(TaskList taskList: lists) {
                mTaskListNames.add(taskList.getName());
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void logTaskLists(String message) {
        Log.d(TAG, message);
        Log.d(TAG, "mTaskListNames.size() = " + mTaskListNames.size());
        for(String s : mTaskListNames) {
            Log.d(TAG, s);
        }
        Log.d(TAG, "");
    }

    public MainController getController() {
        if (D) {
            if (mController == null) {
                Log.d(TAG, "getController null");
            } else {
                Log.d(TAG, "getController ok");
            }

        }

        return mController;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(HaveCurrentTaskListEvent event) {
        Log.d(TAG, "received HaveCurrentTaskListEvent");

        setSupportProgressBarIndeterminateVisibility(false);
        refreshTaskListsUI();
    }

    // MainActivity only uses this event to determine if
    // the trash icon should be shown in the ActionBar
    //
    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(RefreshTaskListEvent event) {
        String taskListName = event.getTaskListName();
        // assume that the given taskList is the one that's being shown on the UI
        determineTrashIconVisibility(taskListName);
    }

    public void onDriveInitialised() {
        /*
         * CURRENT STATE
           - we're on the main thread
           - we have access to drive
           - the 2 json files exist and we have their file ids
         */

        if(D) {
            Log.d(TAG, "onDriveInitialised");
        }

        if(mDriveModel.hasLoadedTaskLists()) {
            // use already loaded data
            if(D) {
                Log.d(TAG, "already loaded data");
            }
            refreshTaskListsUI();
        } else {
            if(D) {
                Log.d(TAG, "launching async tasks to load data");
            }
            // load tasklists if a previous activity hasn't done so
            // this async task will send a HaveCurrentTaskListEvent

            setSupportProgressBarIndeterminateVisibility(true);

            mDriveModel.asyncLoadCurrentTaskLists();
            mDriveModel.asyncLoadHistoricTaskLists();
        }
    }

    private class PageListener extends ViewPager.SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            String currentListName = mTaskListNames.get(position);
            determineTrashIconVisibility(currentListName);
        }
    }

    // only show the trash icon in the action bar if the current TaskList has struck items
    private void determineTrashIconVisibility(String taskListName) {
        TaskList taskList = mController.onGetTaskList(taskListName);
        if(taskList.hasStruckTasks()) {
            showTrashIcon();
        } else {
            hideTrashIcon();
        }
    }

    private void showTrashIcon() {
        Log.d(TAG, "showTrashIcon");
        if(!mShowTrashIcon) {
            mShowTrashIcon = true;
            supportInvalidateOptionsMenu();
        }
    }

    private void hideTrashIcon() {
        Log.d(TAG, "hideTrashIcon");
        if(mShowTrashIcon) {
            mShowTrashIcon = false;
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        if (D) {
            Log.d(TAG, "onCreate");
        }

        mController = new MainController(this, mDriveModel);

        mTaskListNames = new ArrayList<String>();
        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), mTaskListNames);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        PageListener pageListener = new PageListener();
        mIndicator.setOnPageChangeListener(pageListener);

        mDriveDatabase.initialise();
    }

    // Called after onCreate has finished, use to restore UI state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        // Will only be called if the Activity has been
        // killed by the system since it was last visible.
        if (D) {
            Log.d(TAG, "onRestoreInstanceState");
        }

        logTaskLists("onRestoreInstanceState");
    }

    // Called before subsequent visible lifetimes
    // for an Activity process.
    @Override
    public void onRestart() {
        super.onRestart();
        if (D) {
            Log.d(TAG, "onRestart");
        }

        // Load changes knowing that the Activity has already
        // been visible within this process.
        refreshTaskListsUI();
        //new TaskListsAsyncTask(mDriveModel).execute();
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        if (D) {
            Log.d(TAG, "onStart");
        }
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        if (D) {
            Log.d(TAG, "onResume");
        }
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.

        // maybe coming back from a ManageListsActivity in which TaskLists were created/deleted
        //refreshTaskListsUI();

        // Apply any required UI change now that the Activity is visible.
        EventBus.getDefault().register(this);

    }

    // Called to save UI state changes at the
    // end of the active lifecycle.
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
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
        if (D) {
            Log.d(TAG, "onPause");
        }
        EventBus.getDefault().unregister(this);
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
    public void onDestroy() {
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
        if (D) {
            Log.d(TAG, "onDestroy");
        }
        //EventBus.getDefault().unregister(this);
        mController.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        if(mShowTrashIcon) {
            MenuItem mi = menu.add(Menu.NONE, 0, Menu.NONE, R.string.discard_lists);
            mi.setIcon(R.drawable.ic_discard).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            mTrashItemId = mi.getItemId();
        } else {
            menu.removeItem(mTrashItemId);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "clicked " + item);

        if(item.getItemId() == mTrashItemId) {
            mController.onRemoveCompletedTasks(getCurrentTaskListName());
        }

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_manage_lists:
                startManageListsActivity();
                break;

            case R.id.menu_about:
                startAboutActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private TaskList getCurrentTaskList() {
        int i = mPager.getCurrentItem();
        return mController.onGetTaskList(i);
    }

    private String getCurrentTaskListName() {
        int i = mPager.getCurrentItem();
        return (String)mAdapter.getPageTitle(i);
    }

    private void startManageListsActivity() {
        Intent intent = new Intent(this, ManageListsActivity.class);
        startActivity(intent);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
