
package io.indy.octodo;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.io.IOException;
import java.util.List;

import io.indy.octodo.adapter.TaskListPagerAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.DriveJunction;
import io.indy.octodo.model.DriveStorage;
import io.indy.octodo.model.TaskList;

public class MainActivity extends SherlockFragmentActivity {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private TaskListPagerAdapter mAdapter;

    private ViewPager mPager;

    private PageIndicator mIndicator;

    private MainController mController;

    private DriveStorage mDriveStorage;

    private List<TaskList> mTaskLists;

    public void refreshTaskListsUI() {
        if (D) {
            Log.d(TAG, "refreshTaskListsUI");
        }

        List<TaskList> lists = mController.onGetTaskLists();

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

    public static final int REQUEST_ACCOUNT_PICKER = 1;
    public static final int REQUEST_AUTHORIZATION = 2;
    public static final int CAPTURE_IMAGE = 3;

    private static Drive sService;

    private GoogleAccountCredential mCredential;

    public static final String PREFS_FILENAME = "MyPrefsFile";
    public static final String ACCOUNT_NAME = "account_name";


    // the suffix used on json file ids saved as shared preferences
    public final String ID_SUFFIX = ".drive.id";
    public final String CURRENT_JSON = "current.json";
    public final String HISTORIC_JSON = "historic.json";

    public Drive getStaticDriveService() {
        return sService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D) {
            Log.d(TAG, "onCreate");
        }

        if (D) {
            Log.d(TAG, "creating MainController");
        }
        mController = new MainController(this);
        mTaskLists = mController.onGetTaskLists();

        mDriveStorage = new DriveStorage(this);

        mAdapter = new TaskListPagerAdapter(getSupportFragmentManager(), mTaskLists);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        String scope = "https://www.googleapis.com/auth/drive.appdata";
        mCredential = GoogleAccountCredential.usingOAuth2(this, scope);
        Log.d(TAG, "mCredential is " + mCredential);

        String accountName = getAccountNamePreference();
        if(accountName.isEmpty()) {
            // get the preferred google account
            Log.d(TAG, "account name is empty, asking user to choose an account");
            startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            // after getting the result from the above activity we'll call ensureJsonFileExists

        } else {

            // check to make sure we have an accountName and 2 json filenames
            Log.d(TAG, "accountName is " + accountName);
            mCredential.setSelectedAccountName(accountName);
            sService = getDriveService(mCredential);

            if(hasBothJsonFileIdPreferences()) {
                Log.d(TAG, "have both json files");
                // saveFileToDrive();
                // load contents of the 2 json files
                woohoo();

            } else {
                mDriveStorage.ensureJsonFilesExist();
            }
        }

    }

    private String getAccountNamePreference() {
        SharedPreferences settings = getSharedPreferences(PREFS_FILENAME, 0);
        return settings.getString(ACCOUNT_NAME, "");
    }

    private void saveAccountNamePreference(String accountName){
        SharedPreferences settings = getSharedPreferences(PREFS_FILENAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACCOUNT_NAME, accountName);
        editor.commit();
    }

    public String getJsonFileIdPreference(String jsonFilename) {
        SharedPreferences settings = getSharedPreferences(PREFS_FILENAME, 0);
        return settings.getString(jsonFilename + ID_SUFFIX, "");
    }

    // saves the given json file's drive id as a shared preference
    public void saveJsonFileIdPreference(String jsonFilename, String id) {
        SharedPreferences settings = getSharedPreferences(PREFS_FILENAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(jsonFilename + ID_SUFFIX, id);
        editor.commit();
    }

    private boolean hasBothJsonFileIdPreferences() {
        String current = getJsonFileIdPreference(CURRENT_JSON);
        String historic = getJsonFileIdPreference(HISTORIC_JSON);
        if(current.isEmpty() || historic.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        // chosen a google account, does it contain the json files?

                        Log.d(TAG, "account name is " + accountName);
                        saveAccountNamePreference(accountName);
                        mCredential.setSelectedAccountName(accountName);
                        sService = getDriveService(mCredential);

                        mDriveStorage.ensureJsonFilesExist();
                    } else {
                        Log.d(TAG, "must have a valid account name");
                        finish();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                Log.d(TAG, "onActivityResult: request authorization");
                if (resultCode == Activity.RESULT_OK) {
                    // return to ensureJsonFilesExist
                    mDriveStorage.ensureJsonFilesExist();
                } else {
                    startActivityForResult(mCredential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    //saveFileToDrive();
                }
        }
    }


    private void woohoo() {
        Log.d(TAG, "can now get the json and pass it to the database");
    }

    private void saveFileToDrive() {
        Log.d(TAG, "saveFileToDrive");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try { // File's binary content


                    // LIST FILES
                    List<File> files = DriveJunction.listAppDataFiles(sService);

                    Log.d(TAG, "files list length is " + files.size());
                    for(File f : files) {
                        Log.d(TAG, "title: " + f.getTitle());
                        Log.d(TAG, "id:" + f.getId());
                    }
                    Log.d(TAG, "finished retrieving files");




                    // READ A FILE
                    String fileId = "1dHUJZx-sMkPSVG6Lcg-59JlV0lw";
                    File ff = DriveJunction.getFileMetadata(sService, fileId);
                    String jsonContent = DriveJunction.downloadFileAsString(sService, ff);
                    Log.d(TAG, "content of file is " + jsonContent);




                    /*
                    // CREATE A FILE
                    String filename = "temp03.json";
                    String json = "{\"array\": [1,2,3],\"boolean\": true,\"null\": null,\"number\": 123,\"object\": {\"a\": \"b\", \"c\": \"d\",\"e\": \"f\"},\"string\": \"Hello World\"}";
                    File file = DriveJunction.createAppDataJsonFile(sService, filename, json);
                    */

                    /*
                    // File's metadata.
                    File config = new File();
                    String filename = "temp03.json";

                    String json = "{\"array\": [1,2,3],\"boolean\": true,\"null\": null,\"number\": 123,\"object\": {\"a\": \"b\", \"c\": \"d\",\"e\": \"f\"},\"string\": \"Hello World\"}";
                    ByteArrayContent content = ByteArrayContent.fromString("application/json", json);

                    List<ParentReference> parents = new ArrayList<ParentReference>();
                    parents.add(new ParentReference().setId("appdata"));

                    config.setTitle(filename);
                    config.setParents(parents);

                    Log.d(TAG, "about to call sService.files().insert");
                    File file = sService.files().insert(config, content).execute();
                    Log.d(TAG, "called sService.files().insert");
*/


                    /*
                    if (file != null) {
                        showToast("file uploaded: " + file.getTitle());

                        Log.d(TAG, "id: " + file.getId()); // 1dHUJZx-sMkPSVG6Lcg-59JlV0lw
                        Log.d(TAG, "mimetype: " + file.getMimeType()); // application/json
                        Log.d(TAG, "title: " + file.getTitle()); // temp03.json
                        List<ParentReference> par = file.getParents();
                        Log.d(TAG, "size of parentReferences is:" + par.size()); // 1
                        for(ParentReference pr : par) {
                            Log.d(TAG, "parent id is " + pr.getId()); // 1J54mT3DZPd2UgdO-rewnesDNCadn
                        }

                        startCameraIntent();
                    }
                    */



                    // UPDATE file
                    /*
                    Log.d(TAG, "about to update file");
                    String json = "{\"array\": [71,72,73],\"boolean\": false,\"null\": null,\"number\": 42,\"object\": {\"z\": \"y\", \"x\": \"w\",\"v\": \"u\"},\"string\": \"Goodbye World\"}";
                    ByteArrayContent content2 = new ByteArrayContent("application/json", json.getBytes());
                    File config = sService.files().update(ff.getId(), ff, content2).execute();
                    Log.d(TAG, "updated file");
                    logFileMetadata(config, "config");
                    */


                } catch (NullPointerException e) {
                    Log.d(TAG, "null pointer exception");
                    e.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "userrecoverableauthioexception");
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    private void logFileMetadata(File file, String label) {
        Log.d(TAG, "logging metadata for: " + label);
        Log.d(TAG, "id: " + file.getId());
        Log.d(TAG, "mimetype: " + file.getMimeType());
        Log.d(TAG, "title: " + file.getTitle());
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
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
