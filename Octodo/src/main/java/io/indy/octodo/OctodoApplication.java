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
import io.indy.octodo.model.TaskListsPack;

public class OctodoApplication extends Application {

    static private final boolean D = true;
    static private final String TAG = OctodoApplication.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private int mCurrentLoadSource;
    private int mHistoricLoadSource;

    private TaskListsPack mCurrent;
    private TaskListsPack mHistoric;

    @Override
    public void onCreate() {
        super.onCreate();

        mCurrentLoadSource = OctodoModel.NOT_LOADED;
        mHistoricLoadSource = OctodoModel.NOT_LOADED;

        mCurrent = TaskListsPack.buildEmptyTaskListsPack();
        mHistoric = TaskListsPack.buildEmptyTaskListsPack();
    }

    public boolean setCurrentTaskLists(TaskListsPack taskListsPack, int loadSource) {
        mCurrentLoadSource = loadSource;

        if (mCurrent.getModifiedDate().before(taskListsPack.getModifiedDate())) {
            // overwrite the existing current taskLists with these, more recent ones
            mCurrent.setModifiedDate(taskListsPack.getModifiedDate());
            mCurrent.setTaskLists(taskListsPack.getTaskLists());
            return true;
        } else {
            ifd("setCurrentTaskLists: retaining current taskLists");
        }

        return false;
    }

    public void setHistoricTaskLists(TaskListsPack taskListsPack, int loadSource) {
        mHistoricLoadSource = loadSource;

        mHistoric = taskListsPack;
    }

    public List<TaskList> getCurrentTaskLists() {
        return mCurrent.getTaskLists();
    }

    public List<TaskList> getHistoricTaskLists() {
        return mHistoric.getTaskLists();
    }

    public boolean hasLoadedTaskListFrom(int loadSource) {
        //return mCurrentLoadSource == loadSource && mHistoricLoadSource == loadSource;
        return mCurrentLoadSource == loadSource;
    }

}
