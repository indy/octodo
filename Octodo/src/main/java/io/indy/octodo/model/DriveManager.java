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
import android.widget.Toast;

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
import io.indy.octodo.OctodoApplication;

public class DriveManager {

    /*
    re-implement the public methods from SQLDatabase
    store an instance of this class in MainController
    (maybe store MainController in Application?)
     */

    private static final String TAG = "DriveManager";
    private static final boolean D = true;


    public static final int REQUEST_ACCOUNT_PICKER = 1;
    public static final int REQUEST_AUTHORIZATION = 2;
    public static final int CAPTURE_IMAGE = 3;

    private static Drive sService;

    private GoogleAccountCredential mCredential;


    public static final String PREFS_FILENAME = "MyPrefsFile";
    public static final String ACCOUNT_NAME = "account_name";

    // the suffix used on json file ids saved as shared preferences
    public static final String ID_SUFFIX = ".drive.id";
    public static final String CURRENT_JSON = "current.json";
    public static final String HISTORIC_JSON = "historic.json";

    public final String EMPTY_JSON_OBJECT = "{}";

    private DriveBaseActivity mActivity;
    private OctodoApplication mApplication;

    public DriveManager(DriveBaseActivity activity) {
        mActivity = activity;
        mApplication = (OctodoApplication)mActivity.getApplication();
    }

    public void setCurrentTaskLists(List<TaskList> taskLists) {
        mApplication.setCurrentTaskLists(taskLists);
    }

    public void setHistoricTaskLists(List<TaskList> taskLists) {
        mApplication.setHistoricTaskLists(taskLists);
    }

    public List<TaskList> getCurrentTaskLists() {
        return mApplication.getCurrentTaskLists();
    }

    public List<TaskList> getHistoricTaskLists() {
        return mApplication.getHistoricTaskLists();
    }

    public boolean hasLoadedTaskLists() {
        return mApplication.hasTaskLists();
    }

    public void updateFile(String jsonFile, JSONObject jsonObject) {
        Log.d(TAG, "updatefile");

        try {
            String fileId = getJsonFileIdPreference(jsonFile);
            File ff = DriveJunction.getFileMetadata(sService, fileId);


            String json = jsonObject.toString();
            ByteArrayContent content = new ByteArrayContent("application/json", json.getBytes());
            File config = sService.files().update(ff.getId(), ff, content).execute();
            Log.d(TAG, "updated file");
            logFileMetadata(config, jsonFile);

        } catch(IOException e) {
            Log.d(TAG, "getJSON IOException: " + e);
        }
    }

    public static Drive getsService() {
        return sService;
    }

    public JSONObject getJSON(String driveFilename) {

        JSONObject jsonObject = new JSONObject();

        try {
            String fileId = getJsonFileIdPreference(driveFilename);
            File ff = DriveJunction.getFileMetadata(sService, fileId);
            String jsonString = DriveJunction.downloadFileAsString(sService, ff);

            Log.d(TAG, "content of file is " + jsonString);

            jsonObject = new JSONObject(jsonString);

        } catch (IOException e) {
            Log.d(TAG, "getJSON IOException: " + e);
        } catch (JSONException jse) {
            Log.d(TAG, "getJSON JSONException: " + jse);
        }

        return jsonObject;
    }



    public void ensureJsonFilesExist() {
        if (D) {
            Log.d(TAG, "ensureJsonFilesExist");
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // LIST FILES

                    Log.d(TAG, "before listAppDataFiles");
                    List<File> files = DriveJunction.listAppDataFiles(sService);
                    Log.d(TAG, "after listAppDataFiles");

                    Log.d(TAG, "files list length is " + files.size());

                    boolean foundCurrent = false;
                    boolean foundHistoric = false;

                    for(File f : files) {


                        if(f.getTitle().equals(CURRENT_JSON)) {

                            // check that the found json file's id matches the one in shared preferences
                            String id = getJsonFileIdPreference(CURRENT_JSON);
                            if(!id.equals(f.getId())) {
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
                                Log.d(TAG, "saving pre-existing id for " + CURRENT_JSON);
                                saveJsonFileIdPreference(CURRENT_JSON, f.getId());
                            }

                            foundCurrent = true;
                        }
                        if(f.getTitle().equals(HISTORIC_JSON)) {
                            String id = getJsonFileIdPreference(HISTORIC_JSON);
                            if(!id.equals(f.getId())) {
                                Log.d(TAG, "saving pre-existing id for " + HISTORIC_JSON);
                                saveJsonFileIdPreference(HISTORIC_JSON, f.getId());
                            }

                            foundHistoric = true;
                        }
                    }

                    if(!foundCurrent) {
                        String json = EMPTY_JSON_OBJECT;
                        File file = DriveJunction.createAppDataJsonFile(sService, CURRENT_JSON, json);
                        if (file != null) {
                            Log.d(TAG, "saving file id for " + CURRENT_JSON);
                            // save the file's id in local storage
                            saveJsonFileIdPreference(CURRENT_JSON, file.getId());
                            foundCurrent = true;
                        } else {
                            Log.d(TAG, "unable to create AppDataJsonFile: " + CURRENT_JSON);
                        }
                    }
                    if(!foundHistoric) {
                        String json = EMPTY_JSON_OBJECT;
                        File file = DriveJunction.createAppDataJsonFile(sService, HISTORIC_JSON, json);
                        if (file != null) {
                            Log.d(TAG, "saving file id for " + HISTORIC_JSON);
                            saveJsonFileIdPreference(HISTORIC_JSON, file.getId());
                            foundHistoric = true;
                        } else {
                            Log.d(TAG, "unable to create AppDataJsonFile: " + HISTORIC_JSON);
                        }
                    }

                    if(!foundCurrent || !foundHistoric) {
                        Log.d(TAG, "cannot create required json files");
                        // return an error, ask user to check permissions or launch account picker activity?
                        // exit the application

                    } else {
                        // the shared preferences now have the ids of the 2 json files
                        // get their content and pass it into the database
                        Log.d(TAG, "have file ids for both historic and current");

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.onDriveInitialised();
                            }
                        });
                    }

                } catch (NullPointerException e) {
                    Log.d(TAG, "null pointer exception");
                    e.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "userrecoverableauthioexception");
                    mActivity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
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

    private void saveAccountNamePreference(String accountName){
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
        if(current.isEmpty() || historic.isEmpty()) {
            return false;
        }
        return true;
    }

    public void initialise() {
        String scope = "https://www.googleapis.com/auth/drive.appdata";
        mCredential = GoogleAccountCredential.usingOAuth2(mActivity, scope);
        Log.d(TAG, "mCredential is " + mCredential);

        String accountName = getAccountNamePreference();
        if(accountName.isEmpty()) {
            // get the preferred google account
            Log.d(TAG, "account name is empty, asking user to choose an account");
            mActivity.startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            // after getting the result from the above activity we'll call ensureJsonFileExists

        } else {

            // check to make sure we have an accountName and 2 json filenames
            Log.d(TAG, "accountName is " + accountName);
            mCredential.setSelectedAccountName(accountName);
            sService = getDriveService(mCredential);

            if(hasBothJsonFileIdPreferences()) {
                Log.d(TAG, "have both json files");
                // saveFileToDrive();
                // load contents of the 2 json files
                mActivity.onDriveInitialised();

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

                        Log.d(TAG, "account name is " + accountName);
                        saveAccountNamePreference(accountName);
                        mCredential.setSelectedAccountName(accountName);
                        sService = getDriveService(mCredential);
                        Log.d(TAG, "REQUEST_ACCOUNT_PICKER result calling ensureJsonFilesExist");
                        ensureJsonFilesExist();
                    } else {
                        Log.d(TAG, "must have a valid account name");
                        mActivity.finish();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                Log.d(TAG, "onActivityResult: request authorization");
                if (resultCode == Activity.RESULT_OK) {
                    // return to ensureJsonFilesExist
                    Log.d(TAG, "REQUEST_AUTHORIZATION result calling ensureJsonFilesExist");
                    ensureJsonFilesExist();
                } else {
                    mActivity.startActivityForResult(mCredential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
                break;
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    //saveFileToDrive();
                }
        }
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                credential).build();
    }

    private void saveFileToDrive() {
        Log.d(TAG, "saveFileToDrive");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try { // File's binary content


                    // LIST FILES
                    List<File> files = DriveJunction.listAppDataFiles(sService);

                    Log.d(TAG, "files list length is " + files.size());
                    for(File f : files) {
                        Log.d(TAG, "title: " + f.getTitle());
                        Log.d(TAG, "id:" + f.getId());
                    }
                    Log.d(TAG, "finished retrieving files");




                    // READ A FILE
                    String fileId = "1dHUJZx-sMkPSVG6Lcg-59JlV0lw";
                    File ff = DriveJunction.getFileMetadata(sService, fileId);
                    String jsonContent = DriveJunction.downloadFileAsString(sService, ff);
                    Log.d(TAG, "content of file is " + jsonContent);




                    /*
                    // CREATE A FILE
                    String filename = "temp03.json";
                    String json = "{\"array\": [1,2,3],\"boolean\": true,\"null\": null,\"number\": 123,\"object\": {\"a\": \"b\", \"c\": \"d\",\"e\": \"f\"},\"string\": \"Hello World\"}";
                    File file = DriveJunction.createAppDataJsonFile(sService, filename, json);
                    */

                    /*
                    // File's metadata.
                    File config = new File();
                    String filename = "temp03.json";

                    String json = "{\"array\": [1,2,3],\"boolean\": true,\"null\": null,\"number\": 123,\"object\": {\"a\": \"b\", \"c\": \"d\",\"e\": \"f\"},\"string\": \"Hello World\"}";
                    ByteArrayContent content = ByteArrayContent.fromString("application/json", json);

                    List<ParentReference> parents = new ArrayList<ParentReference>();
                    parents.add(new ParentReference().setId("appdata"));

                    config.setTitle(filename);
                    config.setParents(parents);

                    Log.d(TAG, "about to call sService.files().insert");
                    File file = sService.files().insert(config, content).execute();
                    Log.d(TAG, "called sService.files().insert");
*/


                    /*
                    if (file != null) {
                        showToast("file uploaded: " + file.getTitle());

                        Log.d(TAG, "id: " + file.getId()); // 1dHUJZx-sMkPSVG6Lcg-59JlV0lw
                        Log.d(TAG, "mimetype: " + file.getMimeType()); // application/json
                        Log.d(TAG, "title: " + file.getTitle()); // temp03.json
                        List<ParentReference> par = file.getParents();
                        Log.d(TAG, "size of parentReferences is:" + par.size()); // 1
                        for(ParentReference pr : par) {
                            Log.d(TAG, "parent id is " + pr.getId()); // 1J54mT3DZPd2UgdO-rewnesDNCadn
                        }

                        startCameraIntent();
                    }
                    */



                    // UPDATE file
                    /*
                    Log.d(TAG, "about to update file");
                    String json = "{\"array\": [71,72,73],\"boolean\": false,\"null\": null,\"number\": 42,\"object\": {\"z\": \"y\", \"x\": \"w\",\"v\": \"u\"},\"string\": \"Goodbye World\"}";
                    ByteArrayContent content2 = new ByteArrayContent("application/json", json.getBytes());
                    File config = sService.files().update(ff.getId(), ff, content2).execute();
                    Log.d(TAG, "updated file");
                    logFileMetadata(config, "config");
                    */


                } catch (NullPointerException e) {
                    Log.d(TAG, "null pointer exception");
                    e.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "userrecoverableauthioexception");
                    mActivity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    private void logFileMetadata(File file, String label) {
        Log.d(TAG, "logging metadata for: " + label);
        Log.d(TAG, "id: " + file.getId());
        Log.d(TAG, "mimetype: " + file.getMimeType());
        Log.d(TAG, "title: " + file.getTitle());
    }

    public void showToast(final String toast) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCameraIntent() {
        Log.d(TAG, "startCameraIntent");
        /*
         * String mediaStorageDir =
         * Environment.getExternalStoragePublicDirectory(
         * Environment.DIRECTORY_PICTURES).getPath(); String timeStamp = new
         * SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
         * fileUri = Uri.fromFile(new java.io.File(mediaStorageDir +
         * java.io.File.separator + "IMG_" + timeStamp + ".jpg")); Intent
         * cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         * cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
         * startActivityForResult(cameraIntent, CAPTURE_IMAGE);
         */
    }

}
