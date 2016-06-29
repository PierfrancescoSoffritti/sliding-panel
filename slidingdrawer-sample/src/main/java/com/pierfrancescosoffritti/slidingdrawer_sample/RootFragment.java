package com.pierfrancescosoffritti.slidingdrawer_sample;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawerContainer;
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class RootFragment extends Fragment {

    private TabLayout tabs;
    private ViewPagerAdapter mPagerAdapter;

    public RootFragment() {
        // Required empty public constructor
    }

    private static SlidingDrawerContainer mSlidingDrawerContainer;

    public static RootFragment newInstance(SlidingDrawerContainer slidingDrawerContainer) {
        mSlidingDrawerContainer = slidingDrawerContainer;
        return new RootFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_root, container, false);

        tabs = (TabLayout) view.findViewById(R.id.tab_layout);

        ListFragment listFragment1 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), "TAG1");
        ListFragment listFragment2 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), "TAG2");

        setupViewPager(
                view,
                tabs,
                new Pair(listFragment1, "list1"),
                new Pair(listFragment2, "list2")
        );

        mSlidingDrawerContainer.setDragView(tabs);

        return view;
    }

    private void setupViewPager(View view, TabLayout tabs, Pair<Fragment, String>... fragments) {
        mPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        tabs.setupWithViewPager(mViewPager);
    }
}
