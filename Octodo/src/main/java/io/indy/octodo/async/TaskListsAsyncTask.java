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

package io.indy.octodo.async;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.drive.Drive;

import org.json.JSONObject;

import java.util.List;

import io.indy.octodo.MainActivity;
import io.indy.octodo.model.DriveStorage;
import io.indy.octodo.model.TaskList;


// get the current tasklists from drive

public class TaskListsAsyncTask extends AsyncTask<Void, Void, List<TaskList>> {

    private final DriveStorage mDriveStorage;
    private final MainActivity mActivity; // TODO: change from MainActivity to the OctodoGDriveActivityThingy

    // TODO: check chris bane's photup app for usage on WeakReferences in AsyncTasks

    public TaskListsAsyncTask(MainActivity activity, DriveStorage driveStorage) {
        mActivity = activity;
        mDriveStorage = driveStorage;
    }

    @Override
    protected List<TaskList> doInBackground(Void... params) {
        // get the current tasklists from the json files on drive and deserialise them into TaskLists

        JSONObject jsonObject = mDriveStorage.getJSON(DriveStorage.CURRENT_JSON);
        return DriveStorage.fromJSON(jsonObject);
    }


    @Override
    protected void onPostExecute(List<TaskList> result) {
        super.onPostExecute(result);

        // populate the current values in drive storage
        mDriveStorage.setCurrentTaskLists(result);
        // update the UI
        mActivity.haveCurrentTaskLists();

    }

}
