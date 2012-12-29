package io.indy.octodo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

class MainFragmentAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    protected static final String[] CONTENT = new String[] { "Current",
            "Completed", "Infographic" };

    private int mCount = CONTENT.length;

    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
        if (D)
            Log.d(TAG, "Constructor");
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MainFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}