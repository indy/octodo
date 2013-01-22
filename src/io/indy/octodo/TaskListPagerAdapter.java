package io.indy.octodo;

import io.indy.octodo.model.TaskList;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

class TaskListPagerAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private List<TaskList> mLists;

    public TaskListPagerAdapter(FragmentManager fm, List<TaskList> lists) {
        super(fm);
        if (D)
            Log.d(TAG, "Constructor");

        mLists = lists;
    }

    @Override
    public Fragment getItem(int position) {
        TaskList taskList = getTaskList(position);
        return TaskListFragment.newInstance(taskList);
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TaskList taskList = getTaskList(position);
        return taskList.getName();
    }

    public TaskList getTaskList(int position) {
        return mLists.get(position);
    }
}
