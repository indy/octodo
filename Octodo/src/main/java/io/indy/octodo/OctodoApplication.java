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

import android.app.Application;
import android.util.Log;

import java.util.List;

import io.indy.octodo.model.OctodoModel;
import io.indy.octodo.model.TaskList;

public class OctodoApplication extends Application {

    static private final boolean D = true;
    static private final String TAG = OctodoApplication.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    private List<TaskList> mTaskLists;
    private List<TaskList> mHistoricTaskLists;

    private int mCurrentLoadSource;
    private int mHistoricLoadSource;

    @Override
    public void onCreate() {
        super.onCreate();

        mTaskLists = null;
        mHistoricTaskLists = null;
        mCurrentLoadSource = OctodoModel.NOT_LOADED;
        mHistoricLoadSource = OctodoModel.NOT_LOADED;
    }

    public void setCurrentTaskLists(List<TaskList> taskLists, int loadSource) {
        mTaskLists = taskLists;
        mCurrentLoadSource = loadSource;
    }

    public void setHistoricTaskLists(List<TaskList> taskLists, int loadSource) {
        mHistoricTaskLists = taskLists;
        mHistoricLoadSource = loadSource;
    }

    public List<TaskList> getCurrentTaskLists() {
        return mTaskLists;
    }

    public List<TaskList> getHistoricTaskLists() {
        return mHistoricTaskLists;
    }

    public boolean hasTaskLists() {
        return (mTaskLists != null && mHistoricTaskLists != null);
    }

    public boolean hasLoadedTaskListFrom(int loadSource) {
        //return mCurrentLoadSource == loadSource && mHistoricLoadSource == loadSource;
        return mCurrentLoadSource == loadSource;
    }

}
