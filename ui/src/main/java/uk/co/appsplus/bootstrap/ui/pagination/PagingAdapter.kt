package uk.co.appsplus.bootstrap.ui.pagination

import androidx.recyclerview.widget.RecyclerView

abstract class PagingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isLoading = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (isLoading) 1 else 0
}
