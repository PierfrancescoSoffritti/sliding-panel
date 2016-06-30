package com.pierfrancescosoffritti.slidingdrawer_sample;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.slidingdrawer_sample.adapters.ViewPagerAdapter;
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class RootFragment extends Fragment implements SlidingDrawer.OnSlideListener {

    private final static String TAG1 = "TAG1";
    private final static String TAG2 = "TAG2";

    private View collapsedContent;
    private View expandedContent;

    private TabLayout tabs;
    private ViewPager viewPager;

    public RootFragment() {
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

        collapsedContent = view.findViewById(R.id.sliding_drawer_collapsed_view);
        expandedContent = view.findViewById(R.id.expanded_content);

        tabs = (TabLayout) view.findViewById(R.id.tab_layout);

        ListFragment listFragment1 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), TAG1);
        ListFragment listFragment2 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), TAG2);

        setupViewPager(
                view,
                tabs,
                new Pair(listFragment1, "list1"),
                new Pair(listFragment2, "list2")
        );

        mSlidingDrawerContainer.setDragView(collapsedContent);

        return view;
    }

    @SafeVarargs
    private final void setupViewPager(View view, TabLayout tabs, Pair<Fragment, String>... fragments) {
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
        tabs.setAlpha(currentSlide);
        viewPager.setAlpha(currentSlide);

        if(currentSlide == 0) {
            collapsedContent.setVisibility(View.VISIBLE);
            expandedContent.setVisibility(View.INVISIBLE);
            slidingDrawer.setDraggableView(collapsedContent);
        }else if (currentSlide == 1) {
            collapsedContent.setVisibility(View.INVISIBLE);
            expandedContent.setVisibility(View.VISIBLE);
            slidingDrawer.setDraggableView(tabs);
        } else {
            collapsedContent.setVisibility(View.VISIBLE);
            expandedContent.setVisibility(View.VISIBLE);
        }
    }
}
