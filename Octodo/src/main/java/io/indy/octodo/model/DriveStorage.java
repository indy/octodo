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

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.indy.octodo.DriveBaseActivity;
import io.indy.octodo.async.UpdateTaskListsAsyncTask;

public class DriveStorage {
    static private final boolean D = true;
    static private final String TAG = DriveStorage.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    public static final int REQUEST_ACCOUNT_PICKER = 1;
    public static final int REQUEST_AUTHORIZATION = 2;

    private static Drive sService;

    private GoogleAccountCredential mCredential;

    public static final String PREFS_FILENAME = "MyPrefsFile";
    public static final String ACCOUNT_NAME = "account_name";

    // the suffix used on json file ids saved as shared preferences
    public static final String ID_SUFFIX = ".drive.id";
    public static final String CURRENT_JSON = "current.json";
    public static final String HISTORIC_JSON = "historic.json";

    public final String EMPTY_JSON_OBJECT = "{}";

    private static final String HEADER = "header";
    private static final String BODY = "body";

    private DriveBaseActivity mActivity;

    public DriveStorage(DriveBaseActivity activity) {
        mActivity = activity;
    }

    public void updateFile(String jsonFile, JSONObject jsonObject) {
        ifd("updatefile");

        try {
            String fileId = getJsonFileIdPreference(jsonFile);
            File ff = DriveJunction.getFileMetadata(sService, fileId);


            String json = jsonObject.toString();
            ByteArrayContent content = new ByteArrayContent("application/json", json.getBytes());
            File config = sService.files().update(ff.getId(), ff, content).execute();

            if (D) {
                ifd("updated file");
                logFileMetadata(config, jsonFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSON(String driveFilename) {

        JSONObject jsonObject = new JSONObject();

        try {
            String fileId = getJsonFileIdPreference(driveFilename);
            File ff = DriveJunction.getFileMetadata(sService, fileId);
            String jsonString = DriveJunction.downloadFileAsString(sService, ff);
            //ifd("content of " + driveFilename + " is " + jsonString);

            jsonObject = new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    public void ensureJsonFilesExist() {
        ifd("ensureJsonFilesExist");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // LIST FILES
                    List<File> files = DriveJunction.listAppDataFiles(sService);

                    ifd("files list length is " + files.size());

                    boolean foundCurrent = false;
                    boolean foundHistoric = false;

                    for (File f : files) {
                        if (f.getTitle().equals(CURRENT_JSON)) {

                            // check that the found json file's id matches the one in shared preferences
                            String id = getJsonFileIdPreference(CURRENT_JSON);
                            if (!id.equals(f.getId())) {
                                /*
                                Scenarios to get here:

                                1. user started using app on machine A
                                2. user started using app on machine B <- we hit this code path

                                _or_

                                1. user started using app on machine A
                                2. delete app data using web interface to Google Drive
                                3. then used app on machine B <- new ids will be made for current and historic
                                4. then went back to machine A <- we hit this code path - machine A will have the old ids stored in it's shared preferences

                                treat the drive version as the canonical truth, overwrite the id stored in shared preferences with the one on drive

                                 */
                                ifd("saving pre-existing id for " + CURRENT_JSON);
                                saveJsonFileIdPreference(CURRENT_JSON, f.getId());
                            }

                            foundCurrent = true;
                        }
                        if (f.getTitle().equals(HISTORIC_JSON)) {
                            String id = getJsonFileIdPreference(HISTORIC_JSON);
                            if (!id.equals(f.getId())) {
                                ifd("saving pre-existing id for " + HISTORIC_JSON);
                                saveJsonFileIdPreference(HISTORIC_JSON, f.getId());
                            }

                            foundHistoric = true;
                        }
                    }

                    if (!foundCurrent) {
                        String json = EMPTY_JSON_OBJECT;
                        File file = DriveJunction.createAppDataJsonFile(sService, CURRENT_JSON, json);
                        if (file != null) {
                            ifd("saving file id for " + CURRENT_JSON);
                            // save the file's id in local storage
                            saveJsonFileIdPreference(CURRENT_JSON, file.getId());
                            foundCurrent = true;
                        } else {
                            ifd("unable to create AppDataJsonFile: " + CURRENT_JSON);
                        }
                    }
                    if (!foundHistoric) {
                        String json = EMPTY_JSON_OBJECT;
                        File file = DriveJunction.createAppDataJsonFile(sService, HISTORIC_JSON, json);
                        if (file != null) {
                            ifd("saving file id for " + HISTORIC_JSON);
                            saveJsonFileIdPreference(HISTORIC_JSON, file.getId());
                            foundHistoric = true;
                        } else {
                            ifd("unable to create AppDataJsonFile: " + HISTORIC_JSON);
                        }
                    }

                    if (!foundCurrent || !foundHistoric) {
                        ifd("cannot create required json files");
                        // return an error, ask user to check permissions or launch account picker activity?
                        // exit the application

                    } else {
                        // the shared preferences now have the ids of the 2 json files
                        // get their content and pass it into the database
                        ifd("have file ids for both historic and current");

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.onDriveDatabaseInitialised();
                            }
                        });
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    e.printStackTrace();
                    mActivity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    private String getAccountNamePreference() {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_FILENAME, 0);
        return settings.getString(ACCOUNT_NAME, "");
    }

    private void saveAccountNamePreference(String accountName) {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_FILENAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACCOUNT_NAME, accountName);
        editor.commit();
    }

    public String getJsonFileIdPreference(String jsonFilename) {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_FILENAME, 0);
        return settings.getString(jsonFilename + ID_SUFFIX, "");
    }

    // saves the given json file's drive id as a shared preference
    private void saveJsonFileIdPreference(String jsonFilename, String id) {
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_FILENAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(jsonFilename + ID_SUFFIX, id);
        editor.commit();
    }

    private boolean hasBothJsonFileIdPreferences() {
        String current = getJsonFileIdPreference(CURRENT_JSON);
        String historic = getJsonFileIdPreference(HISTORIC_JSON);
        if (current.isEmpty() || historic.isEmpty()) {
            return false;
        }
        return true;
//        return !current.isEmpty() && !historic.isEmpty()
    }

    public void initialise() {
        String scope = "https://www.googleapis.com/auth/drive.appdata";
        mCredential = GoogleAccountCredential.usingOAuth2(mActivity, scope);
        ifd("mCredential is " + mCredential);

        String accountName = getAccountNamePreference();
        if (accountName.isEmpty()) {
            // get the preferred google account
            ifd("account name is empty, asking user to choose an account");
            mActivity.startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            // after getting the result from the above activity we'll call ensureJsonFileExists

        } else {

            // check to make sure we have an accountName and 2 json filenames
            ifd("accountName is " + accountName);
            mCredential.setSelectedAccountName(accountName);
            sService = getDriveService(mCredential);

            if (hasBothJsonFileIdPreferences()) {
                ifd("have both json files");
                mActivity.onDriveDatabaseInitialised();
            } else {
                ensureJsonFilesExist();
            }
        }
    }


    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        // chosen a google account, does it contain the json files?

                        ifd("account name is " + accountName);
                        saveAccountNamePreference(accountName);
                        mCredential.setSelectedAccountName(accountName);
                        sService = getDriveService(mCredential);
                        ifd("REQUEST_ACCOUNT_PICKER result calling ensureJsonFilesExist");
                        ensureJsonFilesExist();
                    } else {
                        ifd("must have a valid account name");
                        mActivity.finish();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                ifd("onActivityResult: request authorization");
                if (resultCode == Activity.RESULT_OK) {
                    // return to ensureJsonFilesExist
                    ifd("REQUEST_AUTHORIZATION result calling ensureJsonFilesExist");
                    ensureJsonFilesExist();
                } else {
                    mActivity.startActivityForResult(mCredential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                credential).build();
    }

    private void logFileMetadata(File file, String label) {
        ifd("logging metadata for: " + label);
        ifd("id: " + file.getId());
        ifd("mimetype: " + file.getMimeType());
        ifd("title: " + file.getTitle());
    }

    public void saveCurrentTaskLists(JSONObject json) {
        // launch an asyncTask that updates the current json file
        new UpdateTaskListsAsyncTask(this, CURRENT_JSON, json).execute();
    }

    public void saveHistoricTaskLists(JSONObject json) {
        new UpdateTaskListsAsyncTask(this, HISTORIC_JSON, json).execute();
    }

}
