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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.LoadedTaskListsEvent;
import io.indy.octodo.event.PersistDataPostEvent;
import io.indy.octodo.helper.NotificationHelper;
import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class EditTaskActivity extends DriveBaseActivity implements AdapterView.OnItemSelectedListener {

    // debug
    static private final boolean D = true;
    static private final String TAG = EditTaskActivity.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    static public final String INTENT_EXTRA_LIST_NAME = "list name";
    static public final String INTENT_EXTRA_START_TIME = "task started at";

    private MainController mController;
    private OctodoModel mOctodoModel;

    private NotificationHelper mNotification;

    private Task mTask;
    private List<String> mListNames;
    private boolean mSetInitialSpinnerValue;

    private Spinner mSpinner;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifd("onCreate");

        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_edit_task);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNotification = new NotificationHelper(this);

        mSpinner = (Spinner)findViewById(R.id.spinner);
        mEditText = (EditText)findViewById(R.id.editText);

        mSpinner.setOnItemSelectedListener(this);

        Button delete = (Button)findViewById(R.id.button);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickedDelete();
            }
        });
    }



    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        ifd("onStart");

        // Apply any required UI change now that the Activity is visible.
        EventBus.getDefault().register(this);

        mOctodoModel = new OctodoModel(this);
        mController = new MainController(this, mOctodoModel);
        ifd("mController = " + System.identityHashCode(mController));

        if(!mOctodoModel.hasLoadedTaskListFrom(OctodoModel.LOADED_FROM_DRIVE)) {
            ifd("not loaded data from drive");
            mOctodoModel.initFromFile();
        } else {
            ifd("already loaded data from drive");
        }

        init();

        mDriveStorage.initialise();
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


    private void onClickedDelete() {
        ifd("clicked delete");

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.delete_task_title));
        ad.setMessage(getString(R.string.delete_task_message));
        ad.setPositiveButton(getString(R.string.delete_task_positive),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        ifd("pressed the delete button");
                        // close the drawer
                        mController.onTaskDelete(mTask);
                        finish();
                    }
                });

        ad.setNegativeButton(getString(R.string.dialog_generic_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        ifd("pressed the cancel button");
                    }
                });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ifd("pressed cancel");
            }
        });

        ad.show();
    }

    @Override
    public void onDriveDatabaseInitialised() {
        super.onDriveDatabaseInitialised();
        ifd("onDriveDatabaseInitialised");

        mOctodoModel.onDriveDatabaseInitialised();

        if(mOctodoModel.hasLoadedTaskListFrom(OctodoModel.LOADED_FROM_DRIVE)) {
            init();
        } else {
            // MainActivity should have managed the data loading into OctodoModel
            Log.e(TAG, "OctodoModel should have loaded the data");
        }
    }

    private void init() {
        String listName = getIntent().getStringExtra(INTENT_EXTRA_LIST_NAME);
        String startedAt = getIntent().getStringExtra(INTENT_EXTRA_START_TIME);
        mTask = mController.findTask(listName, startedAt);

        mListNames = getListNames();
        mSetInitialSpinnerValue = true;
        populateSpinnerEntries(mListNames);

        refreshUI();
    }

    private List<String> getListNames() {
        List<TaskList> taskLists = mController.onGetTaskLists();
        List<String> listNames = new ArrayList<String>();

        for(TaskList taskList : taskLists) {
            listNames.add(taskList.getName());
        }

        return listNames;
    }

    private void populateSpinnerEntries(List<String> listNames) {
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                listNames
        );
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(aa);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(LoadedTaskListsEvent event) {
        ifd("received LoadedTaskListsEvent");
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(PersistDataPostEvent event) {
        ifd("received savedTaskListsEvent");
        setSupportProgressBarIndeterminateVisibility(false);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ifd("onDestroy");

        mController.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.edit_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home :
                finish();
                break;
            case R.id.menu_cancel :
                finish();
                break;
            case R.id.menu_accept :
                ifd("clicked accept");
                hideKeyboard();
                saveTask();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void saveTask() {

        int spinnerPosition = mSpinner.getSelectedItemPosition();
        String listName = mListNames.get(spinnerPosition);
        String content = mEditText.getText().toString().trim();

        ifd("saveTask: listName: " + listName + ", content: " + content);

        boolean isSaving = mController.onTaskEdited(mTask, content, listName);
        if(isSaving) {
            ifd("isSaving is true");
            setSupportProgressBarIndeterminateVisibility(true);
            mNotification.showInformation(getString(R.string.information_saving_task_changes));
            // now wait for the PersistDataPostEvent
        } else {
            ifd("isSaving is false");
            finish();
        }
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        ifd("onResume");
    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        super.onPause();
        ifd("onPause");
    }

    private void refreshUI() {
        mEditText.setText(mTask.getContent());


    }

    @Override
    public void onItemSelected(AdapterView<?> parent,
                               View v, int position, long id) {

        if(mSetInitialSpinnerValue) {
            mSetInitialSpinnerValue = false;

            // set the spinner's initial value to the task's parent name
            String parentName = mTask.getParentName();
            for(int i=0;i<mListNames.size();i++) {
                if(parentName.equals(mListNames.get(i))) {
                    mSpinner.setSelection(i);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        ifd("nothing selected");
    }
}
