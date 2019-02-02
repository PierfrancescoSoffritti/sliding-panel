package com.pierfrancescosoffritti.slidingdrawer_sample.old;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.slidingdrawer_sample.R;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.adapters.ViewPagerAdapter;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.utils.FragmentsUtils;

public class RootFragment extends Fragment implements SlidingDrawer.OnSlideListener {
    private final static String TAG_1 = "TAG_1";
    private final static String TAG_2 = "TAG_2";

    private ViewPagerAdapter viewPagerAdapter;

    private View collapsedView;
    private View expandedView;

    private TabLayout tabLayout;

    public RootFragment() {
    }

    public static RootFragment newInstance() {
        return new RootFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_root, container, false);

        collapsedView = view.findViewById(R.id.collapsed_view);
        expandedView = view.findViewById(R.id.expanded_view);

        tabLayout = view.findViewById(R.id.tab_layout);

        Fragment listFragment1;
        Fragment listFragment2;

        if(savedInstanceState == null) {
            listFragment1 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), null);
            listFragment2 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), null);
        } else {
            String tag0 = savedInstanceState.getString(TAG_1);
            String tag1 = savedInstanceState.getString(TAG_2);

            listFragment1 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), tag0);
            listFragment2 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), tag1);
        }

        setupViewPager(
                view,
                tabLayout,
                new Pair<>(listFragment1, "name1"),
                new Pair<>(listFragment2, "name2")
        );

        return view;
    }

    @Override
    public void onSaveInstanceState (@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(viewPagerAdapter != null) {
            outState.putString(TAG_1, viewPagerAdapter.getItem(0).getTag());
            outState.putString(TAG_2, viewPagerAdapter.getItem(1).getTag());
        } else {
            outState.putString(TAG_1, TAG_1);
            outState.putString(TAG_2, TAG_2);
        }
    }

    @SafeVarargs
    private final void setupViewPager(View view, TabLayout tabs, Pair<Fragment, String>... fragments) {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
        expandedView.setAlpha(currentSlide);
        slidingDrawer.setDragView(collapsedView);

        if(currentSlide == 0) {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.INVISIBLE);
        }else if (currentSlide == 1) {
            collapsedView.setVisibility(View.INVISIBLE);
            expandedView.setVisibility(View.VISIBLE);

            slidingDrawer.setDragView(tabLayout);
        } else {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.VISIBLE);
        }
    }
}
