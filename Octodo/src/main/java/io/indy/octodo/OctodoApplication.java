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

import java.util.List;

import io.indy.octodo.model.TaskList;

public class OctodoApplication extends Application {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private List<TaskList> mTaskLists;
    private List<TaskList> mHistoricTaskLists;

    @Override
    public void onCreate() {
        super.onCreate();

        mTaskLists = null;
        mHistoricTaskLists = null;
    }

    public void setCurrentTaskLists(List<TaskList> tasklists) {
        mTaskLists = tasklists;
    }

    public void setHistoricTaskLists(List<TaskList> tasklists) {
        mHistoricTaskLists = tasklists;
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

}
