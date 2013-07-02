
package io.indy.octodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.adapter.TaskListPagerAdapter;
import io.indy.octodo.async.HistoricTaskListsAsyncTask;
import io.indy.octodo.async.TaskListsAsyncTask;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.HaveCurrentTaskListEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.DriveManager;
import io.indy.octodo.model.TaskList;

public class MainActivity extends SherlockFragmentActivity {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private TaskListPagerAdapter mAdapter;

    private ViewPager mPager;

    private PageIndicator mIndicator;

    private MainController mController;

    private DriveManager mDriveManager;
    private DriveDatabase mDriveDatabase;

    private List<TaskList> mTaskLists;
    private List<String> mTaskListNames;

    // TODO: check why this is used now
    public void refreshTaskListsUI() {
        if (D) {
            Log.d(TAG, "refreshTaskListsUI");
        }

        List<TaskList> lists = mController.onGetTaskLists();

        mTaskLists.clear();
        mTaskLists.addAll(lists);

        // re-create the list of tasklist names and notify mAdapter of change
        mTaskListNames.clear();
        for(TaskList taskList: mTaskLists) {
            mTaskListNames.add(taskList.getName());
        }
        mAdapter.notifyDataSetChanged();
    }

    private void logTaskLists(String message) {
        Log.d(TAG, message);
        Log.d(TAG, "mTaskLists.size() = " + mTaskLists.size());
        for(TaskList t : mTaskLists) {
            Log.d(TAG, t.getName() + " task size: " + t.getTasks().size());
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

    public void onEvent(HaveCurrentTaskListEvent event) {
        Log.d(TAG, "received HaveCurrentTaskListEvent");
        refreshTaskListsUI();
    }

    public void onDriveInitialised() {
        /*
         * CURRENT STATE
           - we're on the main thread
           - we have access to drive
           - the 2 json files exist and we have their file ids
         */

        Log.d(TAG, "onDriveInitialised");

        new TaskListsAsyncTask(mDriveDatabase).execute();
        //new HistoricTaskListsAsyncTask(mDriveDatabase).execute();

        /*

         * NEXT SET OF ACTIONS
           - parse the contents of the json files into DriveManager
           - populate ui elements with the contents
           - hook up user events to modify model in DriveManager

           --------------------------------------------------

         * PARSE THE CONTENTS OF THE JSON FILES INTO DRIVESTORAGE
           - AsyncTask to get the contents and return TaskLists etc
           - at end of AsyncTask populate ui
         */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D) {
            Log.d(TAG, "onCreate");
        }

        EventBus.getDefault().register(this);

        mDriveManager = new DriveManager(this);
        mDriveDatabase = new DriveDatabase(mDriveManager);

        mController = new MainController(this, mDriveDatabase);


        // mTaskLists will be populated by an event fired from the TaskListsAsyncTask
        mTaskLists = new ArrayList<TaskList>();
        mTaskListNames = new ArrayList<String>();
        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), mTaskListNames);

        Log.d(TAG, "mTaskLists id: " + System.identityHashCode(mTaskLists));

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);


        mDriveManager.initialise();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mDriveManager.onActivityResult(requestCode, resultCode, data);
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
        Log.d(TAG, "mTaskLists id: " + System.identityHashCode(mTaskLists));
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

        logTaskLists("onRestart");
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        if (D) {
            Log.d(TAG, "onStart");
        }
        // Apply any required UI change now that the Activity is visible.
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
        EventBus.getDefault().unregister(this);
        mController.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "clicked " + item);

        switch (item.getItemId()) {
            case R.id.menu_remove_completed_tasks:
                mController.onRemoveCompletedTasks(getCurrentTaskList());
                break;

            case R.id.menu_settings:
                Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_add_task:
                // show the 'add task' ui element in the relevant task list
                // fragment
                mController.onToggleAddTaskForm(getCurrentTaskList());
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
        //return mAdapter.getTaskList(i);
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
