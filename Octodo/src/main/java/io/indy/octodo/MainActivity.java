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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
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
import io.indy.octodo.event.LoadedTaskListsEvent;
import io.indy.octodo.event.PersistDataPostEvent;
import io.indy.octodo.event.PersistDataPreEvent;
import io.indy.octodo.event.RefreshTaskListEvent;
import io.indy.octodo.event.ToggledTaskStateEvent;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.TaskList;

public class MainActivity extends DriveBaseActivity {

    static private final boolean D = true;
    static private final String TAG = MainActivity.class.getSimpleName();

    static void ifd(final String message) {
        if (AppConfig.DEBUG && D) Log.d(TAG, message);
    }

    private TaskListPagerAdapter mAdapter;

    private ViewPager mPager;

    private PageIndicator mIndicator;

    private MainController mController;
    private OctodoModel mOctodoModel;

    private List<String> mTaskListNames;

    private boolean mShowArchiveIcon;

    private int mArchiveItemId;

    private NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        ifd("onCreate");

        mTaskListNames = new ArrayList<String>();
        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), mTaskListNames);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        PageListener pageListener = new PageListener();
        mIndicator.setOnPageChangeListener(pageListener);

        mNotificationHelper = new NotificationHelper(this);

        mDriveStorage.initialise();



        Context context = this.getBaseContext();
        try {
            String name = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            int code = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;

            ifd("package name: " + name);
            ifd("package code: " + code);
        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();

        ifd("onStart");

        mOctodoModel = new OctodoModel(this);
        mController = new MainController(this, mOctodoModel);
        ifd("mController = " + System.identityHashCode(mController));

        mOctodoModel.initFromFile();
        refreshTaskListsUI();

        // Apply any required UI change now that the Activity is visible.
        EventBus.getDefault().register(this);

        onDriveDatabaseInitialised();
    }

    @Override
    public void onDriveDatabaseInitialised() {

        if(!hasDriveCredentials()) {
            return;
        }

        // create mOctodoModel
        super.onDriveDatabaseInitialised();

        // CURRENT STATE
        //  - we're on the main thread
        //  - we have access to drive
        //  - the 2 json files exist and we have their file ids

        ifd("onDriveDatabaseInitialised");

        mOctodoModel.onDriveDatabaseInitialised();

        if (mOctodoModel.hasLoadedTaskListFrom(OctodoModel.LOADED_FROM_DRIVE)) {
            // use already loaded data
            ifd("already loaded data");
            refreshTaskListsUI();
        } else {
            ifd("launching async tasks to load data");

            //mNotificationHelper.showInformation(getString(R.string.information_syncing_data));
            setSupportProgressBarIndeterminateVisibility(true);

            // asynchronously load from drive (will fire a loadedtasklistsevent)
            mOctodoModel.asyncLoadCurrentTaskLists();
            mOctodoModel.asyncLoadHistoricTaskLists();
        }

    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(PersistDataPreEvent event) {
        ifd("received PersistDataPreEvent");
        setSupportProgressBarIndeterminateVisibility(true);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(PersistDataPostEvent event) {
        ifd("received PersistDataPostEvent");
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(LoadedTaskListsEvent event) {
        ifd("received LoadedTaskListsEvent");
        if (event.getLoadSource() == OctodoModel.LOADED_FROM_DRIVE) {
            setSupportProgressBarIndeterminateVisibility(false);
        }
        refreshTaskListsUI();
    }

    // MainActivity only uses this event to determine if
    // the trash icon should be shown in the ActionBar
    //
    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(RefreshTaskListEvent event) {
        String taskListName = event.getTaskListName();
        // assume that the given taskList is the one that's being shown on the UI
        determineArchiveIconVisibility(taskListName);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(ToggledTaskStateEvent event) {
        String taskListName = event.getTaskListName();
        determineArchiveIconVisibility(taskListName);
    }


    // Called after onCreate has finished, use to restore UI state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        // Will only be called if the Activity has been
        // killed by the system since it was last visible.
        ifd("onRestoreInstanceState");

        logTaskLists("onRestoreInstanceState");
    }

    // Called before subsequent visible lifetimes
    // for an Activity process.
    @Override
    public void onRestart() {
        super.onRestart();
        ifd("onRestart");
/*
        // need this check since onRestart() is called during the initial account setup phase
        if(isDriveDatabaseInitialised()) {
            // Load changes knowing that the Activity has already
            // been visible within this process.
            refreshTaskListsUI();
        }
        */
    }


    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        ifd("onResume");
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.

        // possibly coming back from a ManageListsActivity in which TaskLists were deleted
        refreshTaskListsUI();
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
        ifd("onSaveInstanceState");
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
        ifd("onPause");
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
        EventBus.getDefault().unregister(this);
    }

    // Sometimes called at the end of the full lifetime.
    @Override
    public void onDestroy() {
        // Clean up any resources including ending threads,
        // closing database connections etc.
        super.onDestroy();
        ifd("onDestroy");
        //EventBus.getDefault().unregister(this);
        mController.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        if (mShowArchiveIcon) {
            MenuItem mi = menu.add(Menu.NONE, 0, Menu.NONE, R.string.discard_lists);
            //mi.setIcon(R.drawable.ic_action_archive).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            mi.setIcon(R.drawable.ic_action_discard).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            mArchiveItemId = mi.getItemId();
        } else {
            menu.removeItem(mArchiveItemId);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ifd("clicked " + item);

        if (item.getItemId() == mArchiveItemId) {
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
        return (String) mAdapter.getPageTitle(i);
    }

    private void startManageListsActivity() {
        Intent intent = new Intent(this, ManageListsActivity.class);
        startActivity(intent);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }


    private class PageListener extends ViewPager.SimpleOnPageChangeListener {
        public void onPageSelected(int position) {
            String currentListName = mTaskListNames.get(position);
            determineArchiveIconVisibility(currentListName);
        }
    }

    // only show the trash icon in the action bar if the current TaskList has struck items
    private void determineArchiveIconVisibility(String taskListName) {
        TaskList taskList = mController.onGetTaskList(taskListName);
        if (taskList.hasStruckTasks()) {
            showArchiveIcon();
        } else {
            hideArchiveIcon();
        }
    }

    private void showArchiveIcon() {
        ifd("showArchiveIcon");
        if (!mShowArchiveIcon) {
            mShowArchiveIcon = true;
            supportInvalidateOptionsMenu();
        }
    }

    private void hideArchiveIcon() {
        ifd("hideArchiveIcon");
        if (mShowArchiveIcon) {
            mShowArchiveIcon = false;
            supportInvalidateOptionsMenu();
        }
    }

    // TODO: check why this is used now
    public void refreshTaskListsUI() {
        ifd("refreshTaskListsUI");
        ifd("mController = " + System.identityHashCode(mController));

        List<TaskList> lists = mController.onGetTaskLists();

        // re-create the list of tasklist names and notify mAdapter of change
        if (lists != null) {
            mTaskListNames.clear();
            for (TaskList taskList : lists) {
                mTaskListNames.add(taskList.getName());
            }
            mAdapter.notifyDataSetChanged();
            mIndicator.notifyDataSetChanged();
        }
    }

    private void logTaskLists(String message) {
        ifd(message);
        ifd("mTaskListNames.size() = " + mTaskListNames.size());
        for (String s : mTaskListNames) {
            ifd(s);
        }
        ifd("");
    }

    public MainController getController() {
        if (mController == null) {
            ifd("getController null");
        } else {
            ifd("getController ok");
        }
        return mController;
    }
}
