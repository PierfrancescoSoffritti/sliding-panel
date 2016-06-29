package com.pierfrancescosoffritti.slidingdrawer_sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Pair;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Pair<Fragment, String>[] mFragments;

    public ViewPagerAdapter(FragmentManager fm, Pair<Fragment, String>... fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position].first;
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public CharSequence getPageTitle (int position) {
        return mFragments[position].second;
    }
}
