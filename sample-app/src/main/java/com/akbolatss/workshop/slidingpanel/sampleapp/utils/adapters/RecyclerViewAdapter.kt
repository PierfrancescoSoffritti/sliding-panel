package com.akbolatss.workshop.slidingpanel.sampleapp.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.akbolatss.workshop.slidingpanel.sampleapp.R
import com.akbolatss.workshop.slidingpanel.sampleapp.utils.DummyListItems

class RecyclerViewAdapter(private val items: List<DummyListItems.DummyItem>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sample_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
        holder.view.text = items[position].content

        holder.view.setOnClickListener {
            Toast.makeText(it.context, "Clicked: " + items[position].content, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val view: TextView = view as TextView
        var item: DummyListItems.DummyItem? = null

    }
}
