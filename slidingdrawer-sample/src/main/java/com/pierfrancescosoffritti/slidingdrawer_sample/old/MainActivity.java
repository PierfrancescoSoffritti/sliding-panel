package com.pierfrancescosoffritti.slidingdrawer_sample.old;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pierfrancescosoffritti.slidingdrawer.PanelState;
import com.pierfrancescosoffritti.slidingdrawer.SlidingPanel;
import com.pierfrancescosoffritti.slidingdrawer_sample.R;
import com.pierfrancescosoffritti.slidingdrawer_sample.old.fragments.SlidingViewFragment;

public class MainActivity extends AppCompatActivity implements SlidingPanel.OnSlideListener {

    private SlidingPanel slidingPanel;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View emptyView = findViewById(R.id.empty_view);
        slidingPanel = findViewById(R.id.sliding_panel);

        fab = findViewById(R.id.fab);

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(slidingPanel.getState() == PanelState.COLLAPSED) {
                    getSupportActionBar().hide();
                    slidingPanel.setState(PanelState.EXPANDED);
                } else {
                    getSupportActionBar().show();
                    slidingPanel.setState(PanelState.COLLAPSED);
                }
            }
        });

        SlidingViewFragment fragment;
        if (savedInstanceState == null) {
            fragment = new SlidingViewFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.sliding_view, fragment, "TAG").commit();
        } else {
            fragment = (SlidingViewFragment) getSupportFragmentManager().findFragmentByTag("TAG");
        }

        slidingPanel.addSlideListener(fragment);
        slidingPanel.addSlideListener(this);
    }

    @Override
    public void onSlide(SlidingPanel slidingPanel, float currentSlide) {
        if(currentSlide == 0)
            fab.show();
        else
            fab.hide();
    }
}
