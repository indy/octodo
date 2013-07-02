
package io.indy.octodo.adapter;

import io.indy.octodo.TaskListFragment;
import io.indy.octodo.model.TaskList;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

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
