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

package io.indy.octodo.model;

import android.content.Context;
import android.support.v4.util.AtomicFile;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/*
    When the app starts there's a noticeable delay as data from Google Drive is fetched
    over the network, worse if there's no network access then nothing will be displayed

    AtomicStorage acts as a persistent cache which is used to populate the TaskLists at
    startup time.
 */
public class AtomicStorage {

    static private final boolean D = true;
    static private final String TAG = AtomicStorage.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    public static final String CURRENT_FILENAME = "current.json";
    public static final String HISTORIC_FILENAME = "historic.json";

    private Context mContext;

    public AtomicStorage(Context context) {
        mContext = context;
    }

    public JSONObject getJSON(String filename) {
        JSONObject jsonObject = new JSONObject();

        try {
            String jsonString = loadFileAsString(filename);
            if(jsonString == null) {
                ifd("getJSON: unable to load contents of " + filename);
                return null;
            }

            ifd("content of " + filename + " is " + jsonString);

            jsonObject = new JSONObject(jsonString);
        } catch (JSONException jse) {
            ifd("getJSON JSONException: " + jse);
        }

        return jsonObject;
    }

    public void saveJSON(String filename, JSONObject jsonObject) {

        File file = new File(mContext.getFilesDir(), filename);

        AtomicFile atomicFile = new AtomicFile(file);
        FileOutputStream stream = null;
        try {
            stream = atomicFile.startWrite();

            String json = jsonObject.toString();
            byte[] jsonBytes = json.getBytes();

            stream.write(jsonBytes);
            atomicFile.finishWrite(stream);

        } catch(IOException e) {
            ifd("getJSON IOException: " + e);
            if(stream != null) {
                atomicFile.failWrite(stream);
            }
        }
    }

    private String loadFileAsString(String filename) {

        File file = new File(mContext.getFilesDir(), filename);
        if(!file.exists()) {
            ifd("loadFileAsString: " + filename + " does not exist");
            return null;
        }

        try {
            AtomicFile atomicFile = new AtomicFile(file);
            byte[] byteArray = atomicFile.readFully();
            ifd("woohoo loadFileAsString loaded: " + filename);
            return new String(byteArray);
        } catch (IOException e) {
            ifd("IOException " + e);
        }

        ifd("loadFileAsString: failed to read " + filename);
        return null;
    }
}
