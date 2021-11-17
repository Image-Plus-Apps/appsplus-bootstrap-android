package uk.co.appsplus.bootstrap.ui.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addOnScrollToBottomListener(listener: (RecyclerView) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val adapter = recyclerView.adapter ?: return
            when (val layoutManager = recyclerView.layoutManager) {
                is LinearLayoutManager -> {
                    layoutManager
                        .findLastVisibleItemPosition()
                        .takeIf { it == adapter.itemCount - 1 }
                        ?.run { listener(recyclerView) }
                }
                else -> {
                    if (!recyclerView.canScrollVertically(1)) {
                        listener(recyclerView)
                    }
                }
            }
        }
    })
}

fun RecyclerView.addOnScrollToTopListener(listener: (RecyclerView) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            when (val layoutManager = recyclerView.layoutManager) {
                is LinearLayoutManager -> {
                    layoutManager
                        .findFirstVisibleItemPosition()
                        .takeIf { it == 0 }
                        ?.run { listener(recyclerView) }
                }
                else -> {
                    if (!recyclerView.canScrollVertically(-1)) {
                        listener(recyclerView)
                    }
                }
            }
        }
    })
}
