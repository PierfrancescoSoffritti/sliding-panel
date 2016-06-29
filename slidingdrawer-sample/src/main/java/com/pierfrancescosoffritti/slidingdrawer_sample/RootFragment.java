package com.pierfrancescosoffritti.slidingdrawer_sample;


import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class RootFragment extends Fragment implements SlidingDrawer.OnSlideListener {

    private CoordinatorLayout coordinator;

    private TabLayout tabs;
    private ViewPager viewPager;
    private FloatingActionButton fab;

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

        coordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

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
        ViewPagerAdapter mPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(mPagerAdapter);

        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
        tabs.setAlpha(currentSlide);
        viewPager.setAlpha(currentSlide);

        //coordinator.setAlpha(Math.abs(currentSlide-1));

        if(currentSlide == 0) {
            slidingDrawer.setDraggableView(coordinator);
            fab.show();
        } else {
            slidingDrawer.setDraggableView(tabs);
            fab.hide();
        }
    }
}
