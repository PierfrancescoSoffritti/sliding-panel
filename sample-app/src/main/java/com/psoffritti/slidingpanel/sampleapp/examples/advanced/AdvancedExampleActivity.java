package com.psoffritti.slidingpanel.sampleapp.examples.advanced;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.psoffritti.slidingpanel.PanelState;
import com.psoffritti.slidingpanel.SlidingPanel;
import com.psoffritti.slidingpanel.sampleapp.R;
import com.psoffritti.slidingpanel.sampleapp.examples.advanced.fragments.SlidingViewFragment;

public class AdvancedExampleActivity extends AppCompatActivity implements SlidingPanel.OnSlideListener {

    private SlidingPanel slidingPanel;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_example);

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
    public void onSlide(@NonNull SlidingPanel slidingPanel, @NonNull PanelState state, float currentSlide) {
        if(currentSlide == 0)
            fab.show();
        else
            fab.hide();
    }
}
