package com.pierfrancescosoffritti.slidingdrawer_sample;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    private View collapsedView;
    private View expandedView;

    private TabLayout tabLayout;

    @Nullable private SlidingDrawerContainer slidingDrawerContainer;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_root, container, false);

        collapsedView = view.findViewById(R.id.sliding_drawer_collapsed_view);
        expandedView = view.findViewById(R.id.expanded_view);

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        // i'm not handling a lot of stuff, teaching to use Fragments it's not the purpose of this sample :)
        Fragment listFragment1 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(1), null);
        Fragment listFragment2 = FragmentsUtils.findFragment(getChildFragmentManager(), ListFragment.newInstance(2), null);

        setupViewPager(
                view,
                tabLayout,
                new Pair<>(listFragment1, "name1"),
                new Pair<>(listFragment2, "name2")
        );

        assert slidingDrawerContainer != null;
        slidingDrawerContainer.setDragView(collapsedView);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SlidingDrawerContainer) {
            slidingDrawerContainer = (SlidingDrawerContainer) context;
        } else {
            throw new RuntimeException(context.getClass().getSimpleName() +" must implement " +SlidingDrawerContainer.class.getSimpleName());
        }
    }

    @SafeVarargs
    private final void setupViewPager(View view, TabLayout tabs, Pair<Fragment, String>... fragments) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);

        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
        expandedView.setAlpha(currentSlide);

        if(currentSlide == 0) {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.INVISIBLE);

            slidingDrawer.setDragView(collapsedView);
        }else if (currentSlide == 1) {
            collapsedView.setVisibility(View.INVISIBLE);
            expandedView.setVisibility(View.VISIBLE);

            slidingDrawer.setDragView(tabLayout);
        } else {
            collapsedView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.VISIBLE);
        }
    }

    public interface SlidingDrawerContainer {
        void setDragView(View view);
    }
}
