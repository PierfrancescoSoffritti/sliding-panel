package com.psoffritti.slidingpanel.sampleapp.examples.basic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.psoffritti.slidingpanel.sampleapp.R

class BasicExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_example)

//        non_sliding_view.setOnClickListener {
//            if(sliding_panel.getState() == PanelState.EXPANDED)
//                sliding_panel.setState(PanelState.COLLAPSED)
//            else
//                sliding_panel.setState(PanelState.EXPANDED)
//        }

//        sliding_panel.addSlideListener { _, state, _ ->
//        }
    }
}
