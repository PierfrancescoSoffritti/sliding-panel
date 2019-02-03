package com.pierfrancescosoffritti.slidingdrawer_sample.basic_example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pierfrancescosoffritti.slidingdrawer.PanelState
import com.pierfrancescosoffritti.slidingdrawer.SlidingPanel
import com.pierfrancescosoffritti.slidingdrawer_sample.R
import kotlinx.android.synthetic.main.activity_basic_example.*

class BasicExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_example)

        non_sliding_view.setOnClickListener {
            if(sliding_panel.getState() == PanelState.EXPANDED)
                sliding_panel.setState(PanelState.COLLAPSED)
            else
                sliding_panel.setState(PanelState.EXPANDED)
        }

        sliding_panel.addSlideListener { _, state, _ ->
            if(state == PanelState.COLLAPSED)
                fit_to_screen_view.visibility = View.VISIBLE
            else
                fit_to_screen_view.visibility = View.GONE
        }
    }
}
