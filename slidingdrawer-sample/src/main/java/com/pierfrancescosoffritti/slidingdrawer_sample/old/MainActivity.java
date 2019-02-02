package com.pierfrancescosoffritti.slidingdrawer_sample.old;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pierfrancescosoffritti.slidingdrawer.PanelState;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.slidingdrawer_sample.R;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.utils.FragmentsUtils;

public class MainActivity extends AppCompatActivity implements SlidingDrawer.OnSlideListener {

    private SlidingDrawer slidingDrawer;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View emptyView = findViewById(R.id.empty_view);
        slidingDrawer = findViewById(R.id.sliding_panel);

        fab = findViewById(R.id.fab);

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(slidingDrawer.getState() == PanelState.COLLAPSED) {
                    getSupportActionBar().hide();
                    slidingDrawer.setState(PanelState.EXPANDED);
                } else {
                    getSupportActionBar().show();
                    slidingDrawer.setState(PanelState.COLLAPSED);
                }
            }
        });

        RootFragment fragment;
        if(savedInstanceState == null)
            fragment = (RootFragment) FragmentsUtils.swapFragments(getSupportFragmentManager(), R.id.sliding_view, RootFragment.newInstance());
        else
            fragment = (RootFragment) FragmentsUtils.findFragment(getSupportFragmentManager(), RootFragment.newInstance(), null);

        slidingDrawer.addSlideListener(fragment);
        slidingDrawer.addSlideListener(this);
    }

    @Override
    public void onSlide(SlidingDrawer slidingDrawer, float currentSlide) {
        if(currentSlide == 0)
            fab.show();
        else
            fab.hide();
    }
}
