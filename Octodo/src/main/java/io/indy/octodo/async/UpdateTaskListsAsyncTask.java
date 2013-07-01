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

import org.json.JSONObject;

import java.util.List;

import io.indy.octodo.MainActivity;
import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.DriveManager;
import io.indy.octodo.model.TaskList;


// get the current tasklists from drive

public class UpdateTaskListsAsyncTask extends AsyncTask<Void, Void, Void> {

    private final DriveManager mDriveManager;
    private final JSONObject mJSONObject;

    public UpdateTaskListsAsyncTask(DriveManager driveManager, JSONObject jsonObject) {
        mDriveManager = driveManager;
        mJSONObject = jsonObject;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mDriveManager.updateFile(DriveManager.CURRENT_JSON, mJSONObject);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        // update the UI
    }
}