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

import com.actionbarsherlock.view.Menu;

import io.indy.octodo.controller.MainController;
import io.indy.octodo.event.HaveCurrentTaskListEvent;

public class EditTaskActivity extends DriveBaseActivity {

    static private final boolean D = true;
    static private final String TAG = EditTaskActivity.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private MainController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDriveDatabase.initialise();
    }

    @Override
    public void onDriveDatabaseInitialised() {
        super.onDriveDatabaseInitialised();

        ifd("onDriveDatabaseInitialised");

        mController = new MainController(this, mDriveModel);


        if(mDriveModel.hasLoadedTaskLists()) {
            // use already loaded data
//            refreshTaskLists();
        } else {
            // MainActivity should have managed the data loading into DriveModel
            Log.e(TAG, "DriveModel should have loaded the data");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void onEvent(HaveCurrentTaskListEvent event) {
        ifd("received HaveCurrentTaskListEvent");
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
/*
    private void refreshTaskLists() {
        List<TaskList> taskLists = mController.getDeleteableTaskLists();
        mTaskLists.clear();
        mTaskLists.addAll(taskLists);
        mAdapter.notifyDataSetChanged();
    }
    */
}
