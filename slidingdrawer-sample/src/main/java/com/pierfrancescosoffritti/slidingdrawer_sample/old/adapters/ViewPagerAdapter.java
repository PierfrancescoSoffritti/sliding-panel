package com.pierfrancescosoffritti.slidingdrawer_sample.old.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.Pair;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Pair<Fragment, String>[] fragments;

    @SafeVarargs
    public ViewPagerAdapter(FragmentManager fragmentManager, Pair<Fragment, String>... fragments) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position].first;
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle (int position) {
        return fragments[position].second;
    }
}
