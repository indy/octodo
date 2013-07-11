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

import io.indy.octodo.model.Task;

public class MoveTaskEvent {

    private Task mTask;

    private String mOldTaskList;

    private String mNewTaskList;

    public MoveTaskEvent(Task task, String oldTaskList, String newTaskList) {
        mTask = task;
        mOldTaskList = oldTaskList;
        mNewTaskList = newTaskList;
    }

    public String getOldTaskList() {
        return mOldTaskList;
    }

    public String getNewTaskList() {
        return mNewTaskList;
    }
}
