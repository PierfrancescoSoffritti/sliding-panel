package com.akbolatss.workshop.slidingpanel.sampleapp.examples.advanced.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akbolatss.workshop.slidingpanel.sampleapp.R
import com.akbolatss.workshop.slidingpanel.sampleapp.utils.DummyListItem
import com.akbolatss.workshop.slidingpanel.sampleapp.utils.adapters.RecyclerViewAdapter

class ListFragment : Fragment() {

    companion object {

        private const val COLUMN_COUNT = "column-count"

        fun newInstance(columnCount: Int): ListFragment {
            val fragment = ListFragment()

            val args = Bundle()
            args.putInt(COLUMN_COUNT, columnCount)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = inflater.inflate(R.layout.recycler_view, container, false) as RecyclerView

        val columnCount = arguments?.getInt(COLUMN_COUNT) ?: 1

        recyclerView.layoutManager = if (columnCount <= 1)
            LinearLayoutManager(context)
        else
            GridLayoutManager(context, columnCount)

        recyclerView.adapter = RecyclerViewAdapter(DummyListItem.ITEMS)

        return recyclerView
    }
}