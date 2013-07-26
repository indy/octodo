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

import io.indy.octodo.model.DriveStorage;
import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.TaskListsPack;


// get the current tasklists from drive

public class HistoricTaskListsAsyncTask extends AsyncTask<Void, Void, TaskListsPack> {

    private final OctodoModel mOctodoModel;

    public HistoricTaskListsAsyncTask(OctodoModel octodoModel) {
        mOctodoModel = octodoModel;
    }

    @Override
    protected TaskListsPack doInBackground(Void... params) {
        // get the current tasklists from the json files on drive and deserialise them into TaskLists

        DriveStorage driveStorage = mOctodoModel.getDriveStorage();
        JSONObject jsonObject = driveStorage.getJSON(DriveStorage.HISTORIC_JSON);

        return TaskListsPack.fromJSON(jsonObject);
    }

    @Override
    protected void onPostExecute(TaskListsPack result) {
        super.onPostExecute(result);
        mOctodoModel.onLoadedHistoricTaskLists(result, OctodoModel.LOADED_FROM_DRIVE);
    }

}
