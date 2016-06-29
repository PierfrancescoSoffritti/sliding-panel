package com.pierfrancescosoffritti.slidingdrawer_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;
import com.pierfrancescosoffritti.utils.FragmentsUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View target = findViewById(R.id.sample_view);
        SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.sliding_drawer);

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

        ListFragment listFragment = ListFragment.newInstance(1);
        FragmentsUtils.swapFragments(getSupportFragmentManager(), R.id.root, listFragment);

        slidingDrawer.setmScrollableView(listFragment.getScrollableView());
    }
}
