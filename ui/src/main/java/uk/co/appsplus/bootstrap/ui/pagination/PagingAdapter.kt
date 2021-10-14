package uk.co.appsplus.bootstrap.ui.pagination

import androidx.recyclerview.widget.RecyclerView

abstract class PagingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return 1
    }
}
