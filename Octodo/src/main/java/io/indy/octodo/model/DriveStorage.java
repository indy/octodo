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

import android.app.Activity;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.indy.octodo.MainActivity;

public class DriveStorage {

    /*
    re-implement the public methods from Database
    store an instance of this class in MainController
    (maybe store MainController in Application?)
     */

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private MainActivity mActivity;

    public DriveStorage(MainActivity activity) {
        mActivity = activity;
    }

    // Called when you no longer need access to the database.
    public void closeDatabase() {
    }

    public void addList(String name) {
        if (name.equals("")) {
            Log.d(TAG, "attempting to add a tasklist with an empty name");
            return;
        }
    }

    public void deleteList(int id) {
        Log.d(TAG, "deleteList: " + id);
    }

    public void addTask(Task task) {
    }

    public void updateTaskContent(int taskId, String content) {
        if (D) {
            Log.d(TAG, "updateTaskContent id: " + taskId + " content: " + content);
        }
    }

    public void updateTaskState(int taskId, int state) {
        if (D) {
            Log.d(TAG, "updateTaskState id:" + taskId + " state: " + state);
        }
    }

    // re-assign a task to a different tasklist
    public void updateTaskParentList(int taskId, int taskListId) {
        if (D) {
            Log.d(TAG, "updateTaskParentList taskId: " + taskId + " taskListId: " + taskListId);
        }
    }


    public void updateTask(Task task) {
    }

    // DELETE the specified task
    public void deleteTask(int taskId) {
        if (D) {
            Log.d(TAG, "deleteTask: taskId=" + taskId);
        }
    }

    // mark all struck tasks in the tasklist as closed
    public void removeStruckTasks(int taskListId) {
        if (D) {
            Log.d(TAG, "removeStruckTasks: taskListId=" + taskListId);
        }
    }

    // return all the tasks associated with the list
    public List<Task> getTasks(int taskListId) {
        List<Task> res = new ArrayList<Task>();
        return res;
    }

    public List<TaskList> getTaskLists() {
        List<TaskList> res = new ArrayList<TaskList>();
        return res;
    }

    public List<TaskList> getDeleteableTaskLists() {
        List<TaskList> res = new ArrayList<TaskList>();
        return res;
    }



    /* -------------------------------------------------------------- */



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
                    List<File> files = DriveJunction.listAppDataFiles(mActivity.getStaticDriveService());
                    Log.d(TAG, "after listAppDataFiles");

                    Log.d(TAG, "files list length is " + files.size());

                    boolean foundCurrent = false;
                    boolean foundHistoric = false;

                    for(File f : files) {


                        if(f.getTitle().equals(mActivity.CURRENT_JSON)) {

                            // check that the found json file's id matches the one in shared preferences
                            String id = mActivity.getJsonFileIdPreference(mActivity.CURRENT_JSON);
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
                                Log.d(TAG, "saving pre-existing id for " + mActivity.CURRENT_JSON);
                                mActivity.saveJsonFileIdPreference(mActivity.CURRENT_JSON, f.getId());
                            }

                            foundCurrent = true;
                        }
                        if(f.getTitle().equals(mActivity.HISTORIC_JSON)) {
                            String id = mActivity.getJsonFileIdPreference(mActivity.HISTORIC_JSON);
                            if(!id.equals(f.getId())) {
                                Log.d(TAG, "saving pre-existing id for " + mActivity.HISTORIC_JSON);
                                mActivity.saveJsonFileIdPreference(mActivity.HISTORIC_JSON, f.getId());
                            }

                            foundHistoric = true;
                        }
                    }

                    if(!foundCurrent) {
                        String json = "{}";
                        File file = DriveJunction.createAppDataJsonFile(mActivity.getStaticDriveService(), mActivity.CURRENT_JSON, json);
                        if (file != null) {
                            Log.d(TAG, "saving file id for " + mActivity.CURRENT_JSON);
                            // save the file's id in local storage
                            mActivity.saveJsonFileIdPreference(mActivity.CURRENT_JSON, file.getId());
                            foundCurrent = true;
                        } else {
                            Log.d(TAG, "unable to create AppDataJsonFile: " + mActivity.CURRENT_JSON);
                        }
                    }
                    if(!foundHistoric) {
                        String json = "{}";
                        File file = DriveJunction.createAppDataJsonFile(mActivity.getStaticDriveService(), mActivity.HISTORIC_JSON, json);
                        if (file != null) {
                            Log.d(TAG, "saving file id for " + mActivity.HISTORIC_JSON);
                            mActivity.saveJsonFileIdPreference(mActivity.HISTORIC_JSON, file.getId());
                            foundHistoric = true;
                        } else {
                            Log.d(TAG, "unable to create AppDataJsonFile: " + mActivity.HISTORIC_JSON);
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
                        woohoo();
                    }

                } catch (NullPointerException e) {
                    Log.d(TAG, "null pointer exception");
                    e.printStackTrace();
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "userrecoverableauthioexception");
                    mActivity.startActivityForResult(e.getIntent(), MainActivity.REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    private void woohoo() {
        Log.d(TAG, "do some work");
    }
}
