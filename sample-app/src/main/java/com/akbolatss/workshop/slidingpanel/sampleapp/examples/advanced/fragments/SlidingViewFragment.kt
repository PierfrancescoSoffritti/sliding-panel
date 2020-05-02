package com.akbolatss.workshop.slidingpanel.sampleapp.examples.advanced.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.akbolatss.workshop.slidingpanel.PanelState
import com.akbolatss.workshop.slidingpanel.SlidingPanelLayout
import com.akbolatss.workshop.slidingpanel.sampleapp.R
import com.akbolatss.workshop.slidingpanel.sampleapp.utils.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_sliding_view.*
import kotlinx.android.synthetic.main.fragment_sliding_view.view.*

class SlidingViewFragment : Fragment(), SlidingPanelLayout.OnSlideListener {

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sliding_view, container, false)

        val listFragment1: Fragment
        val listFragment2: Fragment

        if (savedInstanceState == null) {
            listFragment1 = ListFragment.newInstance(1)
            listFragment2 = ListFragment.newInstance(2)
        } else {
            val tag0 = savedInstanceState.getString("TAG_0")
            val tag1 = savedInstanceState.getString("TAG_1")

            listFragment1 = fragmentManager?.findFragmentByTag(tag0)!!
            listFragment2 = fragmentManager?.findFragmentByTag(tag1)!!
        }

        setupViewPager(
            view.view_pager,
            view.tab_layout,
            Pair(listFragment1, "name1"),
            Pair(listFragment2, "name2")
        )

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("TAG_0", viewPagerAdapter.getItem(0).tag)
        outState.putString("TAG_1", viewPagerAdapter.getItem(1).tag)
    }

    @SafeVarargs
    private fun setupViewPager(view_pager: ViewPager, tab_layout: TabLayout, vararg fragments: Pair<Fragment, String>) {
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, *fragments)
        view_pager.adapter = viewPagerAdapter
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onSlide(slidingPanel: SlidingPanelLayout, state: PanelState, currentSlide: Float) {
        expanded_view.alpha = currentSlide

        when (currentSlide) {
            0f -> {
                collapsed_view.visibility = View.VISIBLE
                expanded_view.visibility = View.INVISIBLE

                slidingPanel.setDragView(collapsed_view)
            }
            1f -> {
                collapsed_view.visibility = View.INVISIBLE
                expanded_view.visibility = View.VISIBLE

                slidingPanel.setDragView(tab_layout)
            }
            else -> {
                collapsed_view.visibility = View.VISIBLE
                expanded_view.visibility = View.VISIBLE
            }
        }
    }
}