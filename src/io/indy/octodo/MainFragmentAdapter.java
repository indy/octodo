package io.indy.octodo;

import java.util.List;

import io.indy.octodo.model.TaskList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

class MainFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    private List<TaskList> mLists;

    public MainFragmentAdapter(FragmentManager fm, List<TaskList> lists) {
        super(fm);
        if (D)
            Log.d(TAG, "Constructor");

        mLists = lists;
    }

    @Override
    public Fragment getItem(int position) {
        TaskList taskList = mLists.get(position);
        return TestFragment.newInstance(taskList);
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TaskList taskList = mLists.get(position);
        return taskList.getName();
    }
}
