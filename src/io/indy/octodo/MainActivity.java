package io.indy.octodo;

import io.indy.octodo.event.RemoveCompletedTasksEvent;
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

public class MainActivity extends SherlockFragmentActivity
    implements TaskModelInterface {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    TaskListPagerAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    Database mDatabase;

    public void onTaskAdded(Task newTask) {
        mDatabase.addTask(newTask);        
    }

    public void onTaskUpdateState(int taskId, int state) {
        mDatabase.updateTaskState(taskId, state);
    }

    public List<Task> onGetTasks(int taskListId) {
        return mDatabase.getTasks(taskListId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D)
            Log.d(TAG, "onCreate");


        mDatabase = new Database(this);
        List<TaskList> taskLists = mDatabase.getTaskLists();

        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), taskLists);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
                
        // TODO: when to call mDatabase.close() ???
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
        case R.id.menu_discard_tasks:
            
            Log.d(TAG, "removing completed items");
            // remove the completed tasks from the current list
            int i = mPager.getCurrentItem();
            TaskList taskList = mAdapter.getTaskList(i);
            int taskListId = taskList.getId();
            
            // Log.d(TAG, "getCurrentItem returned " + i + " with id: " + taskListId);
            mDatabase.removeStruckTasks(taskListId);
            // refresh the listviewfragment
            // call notifyDataSetChanged() on adapter?

            RemoveCompletedTasksEvent event = new RemoveCompletedTasksEvent(taskListId);
            EventBus.getDefault().post(event);

            break;
        case R.id.menu_settings:
            Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show();
            break;

        case R.id.menu_about:
            startAboutActivity();
            break;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
