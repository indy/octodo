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

package io.indy.octodo.event;

// received a TaskListsPack from a data source
// overwritten: was the data used to overwrite the data in OctodoApplication? (this is done
//              when the received data is newer than the existing data)
// loadSource : where did the TaskListsPack come from

public class LoadedTaskListsEvent {

    private boolean mOverwritten;
    private int mLoadSource;

    public LoadedTaskListsEvent(boolean overwritten, int loadSource) {
        mOverwritten = overwritten;
        mLoadSource = loadSource;
    }

    public int getLoadSource() {
        return mLoadSource;
    }

    public boolean overwritesExistingTaskLists() {
        return mOverwritten;
    }
}
