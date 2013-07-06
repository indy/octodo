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

// event that's broadcast whenever a task is modified
// any UI element that's displaying the task's parent TaskList should refresh

public class RefreshTaskListEvent {

    private String mTaskListName;

    public RefreshTaskListEvent(String taskListName) {
        mTaskListName = taskListName;
    }

    public String getTaskListName() {
        return mTaskListName;
    }
}
