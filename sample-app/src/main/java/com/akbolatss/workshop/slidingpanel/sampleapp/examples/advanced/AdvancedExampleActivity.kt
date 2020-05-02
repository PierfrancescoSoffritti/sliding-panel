package com.akbolatss.workshop.slidingpanel.sampleapp.examples.advanced

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akbolatss.workshop.slidingpanel.PanelState
import com.akbolatss.workshop.slidingpanel.sampleapp.R
import com.akbolatss.workshop.slidingpanel.sampleapp.examples.advanced.fragments.SlidingViewFragment
import kotlinx.android.synthetic.main.activity_advanced_example.*

class AdvancedExampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_example)

        empty_view.setOnClickListener {
            if (sliding_panel.state == PanelState.EXPANDED)
                supportActionBar?.hide()
            else
                supportActionBar?.show()

            sliding_panel.toggle()
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
        sliding_panel.addSlideListener { _, _, currentSlide ->
            if (currentSlide == 0f)
            fab.show()
        else
            fab.hide() }
    }

    override fun onBackPressed() {
        if (sliding_panel.state == PanelState.COLLAPSED)
            sliding_panel.slideTo(PanelState.EXPANDED)
        else
            super.onBackPressed()
    }
}