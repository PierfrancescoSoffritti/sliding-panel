package com.psoffritti.slidingpanel.sampleapp.examples.advanced

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.psoffritti.slidingpanel.PanelState
import com.psoffritti.slidingpanel.sampleapp.R
import com.psoffritti.slidingpanel.sampleapp.examples.advanced.fragments.SlidingViewFragment
import kotlinx.android.synthetic.main.activity_advanced_example.*

class AdvancedExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_example)

        empty_view.setOnClickListener {
            if (sliding_panel.getState() === PanelState.COLLAPSED) {
                supportActionBar?.hide()
                sliding_panel.setState(PanelState.EXPANDED)
            } else {
                supportActionBar?.show()
                sliding_panel.setState(PanelState.COLLAPSED)
            }
        }

        val fragment: SlidingViewFragment
        if (savedInstanceState == null) {
            fragment = SlidingViewFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.sliding_view, fragment, "TAG").commit()
        } else {
            fragment = supportFragmentManager.findFragmentByTag("TAG") as SlidingViewFragment
        }

        sliding_panel.addSlideListener(fragment)
        sliding_panel.addSlideListener{ _, _, currentSlide ->
            if (currentSlide == 0f)
            fab.show()
        else
            fab.hide() }
    }
}