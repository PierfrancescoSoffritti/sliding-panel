package com.akbolatss.workshop.slidingpanel.sampleapp.utils.adapters

import android.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter @SafeVarargs
constructor(
    fragmentManager: FragmentManager,
    private vararg val fragments: Pair<Fragment, String>
) :
    FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

    override fun getItem(position: Int): Fragment {
        return fragments[position].first
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].second
    }
}