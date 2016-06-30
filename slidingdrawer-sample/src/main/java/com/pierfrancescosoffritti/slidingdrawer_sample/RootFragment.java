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
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class RootFragment extends Fragment implements SlidingDrawer.OnSlideListener {

    private final static String TAG1 = "TAG1";
    private final static String TAG2 = "TAG2";

    private ViewPagerAdapter pagerAdapter;

    private View collapsedContent;
    private View expandedContent;

    private TabLayout tabs;
    private ViewPager viewPager;

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

        collapsedContent = view.findViewById(R.id.sliding_drawer_collapsed_view);
        expandedContent = view.findViewById(R.id.expanded_content);

        tabs = (TabLayout) view.findViewById(R.id.tab_layout);

        String tag1 = null, tag2 = null;
        if(savedInstanceState != null) {
            tag1 = savedInstanceState.getString(TAG1);
            tag2 = savedInstanceState.getString(TAG2);
        }

        if(tag1 == null)
            tag1 = TAG1;
        if(tag2 == null)
            tag2 = TAG2;

        ListFragment listFragment1 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), tag1);
        ListFragment listFragment2 = (ListFragment) FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), tag2);

        setupViewPager(
                view,
                tabs,
                new Pair(listFragment1, "list1"),
                new Pair(listFragment2, "list2")
        );

        mSlidingDrawerContainer.setDragView(collapsedContent);

        return view;
    }

    private void setupViewPager(View view, TabLayout tabs, Pair<Fragment, String>... fragments) {
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        // This is necessary if I want to retrieve those fragments instances from the stack.
        // Otherwise the ViewPager will re instantiate the fragments when events like configuration changes occurs, and I won't have any control on them.
        // This could result in a double instantiation of the fragments which will lead to the usual fragment problems.
        outState.putString(TAG1, pagerAdapter.getItem(0).getTag());
        outState.putString(TAG2, pagerAdapter.getItem(1).getTag());
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
