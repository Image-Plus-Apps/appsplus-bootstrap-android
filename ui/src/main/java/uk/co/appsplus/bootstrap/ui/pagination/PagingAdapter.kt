package uk.co.appsplus.bootstrap.ui.pagination

import androidx.recyclerview.widget.RecyclerView

abstract class PagingAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {

    var isLoading = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (isLoading) 1 else 0
}
