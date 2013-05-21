
package io.indy.octodo.adapter;

import io.indy.octodo.TaskListFragment;
import io.indy.octodo.model.TaskList;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class TaskListPagerAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = false;

    private List<TaskList> mTaskLists;

    public TaskListPagerAdapter(FragmentManager fm, List<TaskList> lists) {
        super(fm);
        if (D)
            Log.d(TAG, "Constructor");

        mTaskLists = lists;
    }

    public void updateTaskLists(List<TaskList> lists) {
        if (D) {
            Log.d(TAG, "updateTaskLists");
        }

        mTaskLists.clear();
        mTaskLists.addAll(lists);
    }

    @Override
    public Fragment getItem(int position) {
        if (D) {
            Log.d(TAG, "getItem " + position);
        }

        TaskList taskList = getTaskList(position);
        return TaskListFragment.newInstance(taskList);
    }

    @Override
    public int getCount() {
        if (D) {
            Log.d(TAG, "getCount");
        }

        return mTaskLists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (D) {
            Log.d(TAG, "getPageTitle " + position);
        }

        TaskList taskList = getTaskList(position);
        return taskList.getName();
    }

    public TaskList getTaskList(int position) {
        return mTaskLists.get(position);
    }
}
