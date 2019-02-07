package com.psoffritti.slidingpanel.sampleapp.examples.scrollableview

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.psoffritti.slidingpanel.PanelState
import com.psoffritti.slidingpanel.sampleapp.R
import com.psoffritti.slidingpanel.sampleapp.examples.utils.DummyListItems
import com.psoffritti.slidingpanel.sampleapp.examples.utils.adapters.RecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_basic_horizontal_example.*
import kotlinx.android.synthetic.main.recycler_view.*

class ScrollableViewExampleActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrollable_view_example)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = RecyclerViewAdapter(DummyListItems.ITEMS)

        val formatter = "%.2f"

        sliding_panel.addSlideListener { slidingPanel, state, currentSlide ->
            when(state) {
                PanelState.COLLAPSED -> panel_state_text_view.text = "Sliding view COLLAPSED: ${formatter.format(currentSlide)}"
                PanelState.EXPANDED -> panel_state_text_view.text = "Sliding view EXPANDED: ${formatter.format(currentSlide)}"
                PanelState.SLIDING -> panel_state_text_view.text = "Sliding view SLIDING: ${formatter.format(currentSlide)}"
            }
        }

        recycler_view.alpha = 0f
        sliding_panel.addSlideListener { slidingPanel, state, currentSlide ->
            recycler_view.alpha = currentSlide
        }
    }
}
