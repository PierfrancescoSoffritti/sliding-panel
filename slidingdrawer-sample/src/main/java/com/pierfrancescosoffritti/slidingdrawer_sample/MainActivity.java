package com.pierfrancescosoffritti.slidingdrawer_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawerContainer;
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class MainActivity extends AppCompatActivity implements SlidingDrawerContainer {

    SlidingDrawer slidingDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View target = findViewById(R.id.sample_view);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);

        target.setOnClickListener(new View.OnClickListener() {
            private boolean show = false;

            @Override
            public void onClick(View view) {
                Log.d(getClass().getSimpleName(), "click");

                if(!show)
                    getSupportActionBar().hide();
                else
                    getSupportActionBar().show();

                show = !show;
            }
        });

        RootFragment fragment = RootFragment.newInstance(this);
        FragmentsUtils.swapFragments(getSupportFragmentManager(), R.id.root, fragment);
    }


    @Override
    public void setDragView(View view) {
        slidingDrawer.setDraggableView(view);
    }
}
