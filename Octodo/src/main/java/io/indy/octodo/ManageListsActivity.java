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

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.indy.octodo.adapter.ManageListsAdapter;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.LoadedTaskListsEvent;
import io.indy.octodo.event.ToggledListSelectionEvent;
import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.TaskList;

public class ManageListsActivity extends DriveBaseActivity {

    static private final boolean D = true;
    static private final String TAG = ManageListsActivity.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private List<TaskList> mTaskLists;

    private ManageListsAdapter mAdapter;

    private MainController mController;
    private OctodoModel mOctodoModel;

    private ListView mListView;

    private EditText mEditText;

    private boolean mShowTrashIcon;

    private int mTrashItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_lists);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (ListView)findViewById(R.id.listViewTaskLists);

        mTaskLists = new ArrayList<TaskList>();

        mAdapter = new ManageListsAdapter(this, mTaskLists);

        mEditText = (EditText)findViewById(R.id.editText2);
        setKeyboardListener(mEditText);

        mListView.setAdapter(mAdapter);

        mOctodoModel = new OctodoModel(this);
        mController = new MainController(this, mOctodoModel);

        mOctodoModel.initFromFile();
        refreshTaskLists();

        mDriveStorage.initialise();
    }

    @Override
    public void onDriveDatabaseInitialised() {
        super.onDriveDatabaseInitialised();

        ifd("onDriveDatabaseInitialised");

        mOctodoModel.onDriveDatabaseInitialised();

        if(mOctodoModel.hasLoadedTaskListFrom(OctodoModel.LOADED_FROM_DRIVE)) {
            // use already loaded data
            refreshTaskLists();
        } else {
            // load tasklists if a previous activity hasn't done so
            // this async task will send a LoadedTaskListsEvent
            mOctodoModel.asyncLoadCurrentTaskLists();
        }

    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(LoadedTaskListsEvent event) {
        ifd("received LoadedTaskListsEvent");
        refreshTaskLists();
    }

    // A checkbox next to a list's name has been toggled
    //
    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(ToggledListSelectionEvent event) {
        ifd("received ToggledListSelectionEvent");

        if(isAnyTaskListSelected(mTaskLists)) {
            showTrashIcon();
        } else {
            hideTrashIcon();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) {
            ifd("onDestroy");
        }
        mController.onDestroy();
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        if (D) {
            ifd("onResume");
        }
        // Resume any paused UI updates, threads, or processes required
        // by the Activity but suspended when it was inactive.

        // Apply any required UI change now that the Activity is visible.
        EventBus.getDefault().register(this);

    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground Activity.
        super.onPause();
        if (D) {
            ifd("onPause");
        }
        EventBus.getDefault().unregister(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_manage_lists, menu);

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

        if(item.getItemId() == mTrashItemId) {
            mController.deleteSelectedTaskLists();
            refreshTaskLists();
        } else if(item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private void setKeyboardListener(EditText editText) {

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66 && event.getAction() == 1) {
                    addNewList();
                }
                return false;
            }
        });
    }

    private void addNewList() {
        try {
            String name = mEditText.getText().toString();

            mController.addList(name);
            refreshTaskLists();
            mEditText.setText("");
        } catch(NullPointerException e) {
            ifd("addNewList exception: " + e);
        }
    }

    private void refreshTaskLists() {
        List<TaskList> taskLists = mController.getDeleteableTaskLists();
        mTaskLists.clear();
        mTaskLists.addAll(taskLists);
        mAdapter.notifyDataSetChanged();
    }

    private void showTrashIcon() {
        if(!mShowTrashIcon) {
            mShowTrashIcon = true;
            supportInvalidateOptionsMenu();
        }
    }

    private void hideTrashIcon() {
        if(mShowTrashIcon) {
            mShowTrashIcon = false;
            supportInvalidateOptionsMenu();
        }
    }

    private boolean isAnyTaskListSelected(List<TaskList> taskLists) {
        for(TaskList taskList : taskLists) {
            if(taskList.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
