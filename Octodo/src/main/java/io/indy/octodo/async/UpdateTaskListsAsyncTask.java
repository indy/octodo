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

import de.greenrobot.event.EventBus;
import io.indy.octodo.event.PersistDataPostEvent;
import io.indy.octodo.event.PersistDataPreEvent;
import io.indy.octodo.model.DriveStorage;


// save a list of tasklists, fire a saved tasklists event

public class UpdateTaskListsAsyncTask extends AsyncTask<Void, Void, Void> {

    private final DriveStorage mDriveStorage;
    private final JSONObject mJSONObject;
    private final String mJSONFileName;

    public UpdateTaskListsAsyncTask(DriveStorage driveStorage, String jsonFileName, JSONObject jsonObject) {
        mDriveStorage = driveStorage;
        mJSONObject = jsonObject;
        mJSONFileName = jsonFileName;

        // about to save data to GDrive, fire an event so that the
        // activity can inform the user (i.e. show a progress anim
        // in the action bar)
        //
        PersistDataPreEvent event = new PersistDataPreEvent(mJSONFileName);
        EventBus.getDefault().post(event);
    }

    @Override
    protected Void doInBackground(Void... params) {
        mDriveStorage.updateFile(mJSONFileName, mJSONObject);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        // TODO: check to see if mDriveStorage has successfully updated the file on GoogleDrive

        // send event that the file has been saved
        PersistDataPostEvent event = new PersistDataPostEvent(mJSONFileName);
        EventBus.getDefault().post(event);
    }
}
