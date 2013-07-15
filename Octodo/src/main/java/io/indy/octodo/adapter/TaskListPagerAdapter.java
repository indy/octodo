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

package io.indy.octodo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.List;

import io.indy.octodo.TaskListFragment;

public class TaskListPagerAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = false;

    private List<String> mTaskListNames;

    public TaskListPagerAdapter(FragmentManager fm, List<String> taskListNames) {
        super(fm);
        if (D)
            Log.d(TAG, "Constructor");

        mTaskListNames = taskListNames;
    }

    @Override
    public Fragment getItem(int position) {
        if (D) {
            Log.d(TAG, "getItem " + position);
        }

        String taskListName = mTaskListNames.get(position);
        return TaskListFragment.newInstance(taskListName);
    }

    @Override
    public int getCount() {
        if (D) {
            Log.d(TAG, "getCount");
        }

        return mTaskListNames.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (D) {
            Log.d(TAG, "getPageTitle " + position);
        }

        return mTaskListNames.get(position);
    }
}
