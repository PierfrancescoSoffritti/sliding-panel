package com.pierfrancescosoffritti.slidingdrawer_sample.basic_example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.pierfrancescosoffritti.slidingdrawer.PanelState
import com.pierfrancescosoffritti.slidingdrawer_sample.R
import kotlinx.android.synthetic.main.activity_basic_example.*

class BasicExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_example)

        non_slidable_view.setOnClickListener {
            if(sliding_panel.state == PanelState.EXPANDED)
                sliding_panel.state = PanelState.COLLAPSED
            else sliding_panel.state = PanelState.EXPANDED
        }

        sliding_panel.setDragView(drag_view)
    }
}
