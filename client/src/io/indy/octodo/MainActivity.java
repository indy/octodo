package io.indy.octodo;

import io.indy.octodo.adapter.TaskListPagerAdapter;
import io.indy.octodo.event.RemoveCompletedTasksEvent;
import io.indy.octodo.event.ToggleAddTaskFormEvent;
import io.indy.octodo.model.Database;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;
import io.indy.octodo.model.TaskModelInterface;

import java.util.List;

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

import de.greenrobot.event.EventBus;

public class MainActivity extends SherlockFragmentActivity implements
        TaskModelInterface {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private TaskListPagerAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private Database mDatabase;

    public void onTaskAdded(Task newTask) {
        mDatabase.addTask(newTask);
    }

    public void onTaskUpdateState(int taskId, int state) {
        mDatabase.updateTaskState(taskId, state);
    }

    public List<Task> onGetTasks(int taskListId) {
        return mDatabase.getTasks(taskListId);
    }

    public void refreshTaskListsUI() {
        if (D) {
            Log.d(TAG, "refreshTaskListsUI");
        }

        List<TaskList> lists = mDatabase.getTaskLists();
        mAdapter.updateTaskLists(lists);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D)
            Log.d(TAG, "onCreate");

        mDatabase = new Database(this);
        List<TaskList> taskLists = mDatabase.getTaskLists();

        if (D) {
            Log.d(TAG, "all taskLists:");
            for (TaskList tl : taskLists) {
                Log.d(TAG, tl.getName());
            }
        }

        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(),
                taskLists);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
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
        mDatabase.closeDatabase();
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
            if (D) {
                Log.d(TAG, "removing completed items");
            }

            int taskListId = getTaskListId();
            mDatabase.removeStruckTasks(taskListId);

            RemoveCompletedTasksEvent rctEvent;
            rctEvent = new RemoveCompletedTasksEvent(taskListId);
            EventBus.getDefault().post(rctEvent);

            break;
        case R.id.menu_settings:
            Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show();
            break;

        case R.id.menu_add_task:
            // show the 'add task' ui element in the relevant task list fragment
            int id = getTaskListId();
            ToggleAddTaskFormEvent tatfEvent = new ToggleAddTaskFormEvent(id);
            EventBus.getDefault().post(tatfEvent);
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

    private int getTaskListId() {
        int i = mPager.getCurrentItem();
        TaskList taskList = mAdapter.getTaskList(i);
        int taskListId = taskList.getId();
        return taskListId;
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
