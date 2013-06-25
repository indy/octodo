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

import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.DriveManager;
import io.indy.octodo.model.TaskList;


// get the current tasklists from drive

public class HistoricTaskListsAsyncTask extends AsyncTask<Void, Void, List<TaskList>> {

    private final DriveDatabase mDriveDatabase;

    public HistoricTaskListsAsyncTask(DriveDatabase driveDatabase) {
        mDriveDatabase = driveDatabase;
    }

    @Override
    protected List<TaskList> doInBackground(Void... params) {
        // get the current tasklists from the json files on drive and deserialise them into TaskLists

        DriveManager driveManager = mDriveDatabase.getDriveManager();
        JSONObject jsonObject = driveManager.getJSON(DriveManager.HISTORIC_JSON);
        return DriveDatabase.fromJSON(jsonObject);

    }


    @Override
    protected void onPostExecute(List<TaskList> result) {
        super.onPostExecute(result);

        // populate the current values in drive storage
        mDriveDatabase.setHistoricTaskLists(result);
    }

}
