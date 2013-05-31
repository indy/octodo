
package io.indy.octodo;

import io.indy.octodo.adapter.TaskListPagerAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Database;
import io.indy.octodo.model.TaskList;

import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private TaskListPagerAdapter mAdapter;

    private ViewPager mPager;

    private PageIndicator mIndicator;

    private MainController mController;

    private Database mDatabase;

    private List<TaskList> mTaskLists;

    public void refreshTaskListsUI() {
        if (D) {
            Log.d(TAG, "refreshTaskListsUI");
        }

        List<TaskList> lists = mDatabase.getTaskLists();

        mTaskLists.clear();
        mTaskLists.addAll(lists);

        mAdapter.notifyDataSetChanged();
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

    static final int REQUEST_ACCOUNT_PICKER = 1;

    static final int REQUEST_AUTHORIZATION = 2;

    static final int CAPTURE_IMAGE = 3;

    private static Drive service;

    private GoogleAccountCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D) {
            Log.d(TAG, "onCreate");
        }

        // database lifecycle and configuration is managed by MainActivity
        mDatabase = new Database(this);

        if (D) {
            Log.d(TAG, "creating MainController");
        }
        mController = new MainController(this, mDatabase);
        mTaskLists = mDatabase.getTaskLists();

        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), mTaskLists);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        String scope = "https://www.googleapis.com/auth/drive.appdata";
        credential = GoogleAccountCredential.usingOAuth2(this, scope);
        Log.d(TAG, "credential is " + credential);
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                        Log.d(TAG, "request account picker returned " + service);
                        startCameraIntent();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    saveFileToDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    saveFileToDrive();
                }
        }
    }

    private void saveFileToDrive() {
        Log.d(TAG, "saveFileToDrive");
        /*
         * Thread t = new Thread(new Runnable() {
         * @Override public void run() { try { // File's binary content
         * java.io.File fileContent = new java.io.File(fileUri.getPath());
         * FileContent mediaContent = new FileContent("image/jpeg",
         * fileContent); // File's metadata. File body = new File();
         * body.setTitle(fileContent.getName()); body.setMimeType("image/jpeg");
         * File file = service.files().insert(body, mediaContent).execute(); if
         * (file != null) { showToast("Photo uploaded: " + file.getTitle());
         * startCameraIntent(); } } catch (UserRecoverableAuthIOException e) {
         * startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION); } catch
         * (IOException e) { e.printStackTrace(); } } }); t.start();
         */
    }

    private void startCameraIntent() {
        Log.d(TAG, "startCameraIntent");
        /*
         * String mediaStorageDir =
         * Environment.getExternalStoragePublicDirectory(
         * Environment.DIRECTORY_PICTURES).getPath(); String timeStamp = new
         * SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
         * fileUri = Uri.fromFile(new java.io.File(mediaStorageDir +
         * java.io.File.separator + "IMG_" + timeStamp + ".jpg")); Intent
         * cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         * cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
         * startActivityForResult(cameraIntent, CAPTURE_IMAGE);
         */
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                credential).build();
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
        mController.cancelAllNotifications();
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
                mController.onToggleAddTaskForm(getCurrentTaskListId());
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

    public TaskList getTaskList(int id) {
        for (TaskList taskList : mTaskLists) {
            if (taskList.getId() == id) {
                return taskList;
            }
        }
        return null;
    }

    private int getCurrentTaskListId() {
        TaskList taskList = getCurrentTaskList();
        return taskList.getId();
    }

    private TaskList getCurrentTaskList() {
        int i = mPager.getCurrentItem();
        return mAdapter.getTaskList(i);
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
