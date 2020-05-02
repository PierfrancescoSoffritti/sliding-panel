package com.akbolatss.workshop.slidingpanel.sampleapp.utils

import java.util.ArrayList

object DummyListItem {

    private const val COUNT = 25
    val ITEMS: MutableList<DummyItem> = ArrayList()

    init {
        for (i in 1..COUNT)
            ITEMS.add(DummyItem("Item $i"))
    }

    class DummyItem internal constructor(val content: String) {
        override fun toString(): String {
            return content
        }
    }
}