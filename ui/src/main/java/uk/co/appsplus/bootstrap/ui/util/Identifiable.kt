package uk.co.appsplus.bootstrap.ui.util

import androidx.recyclerview.widget.DiffUtil

interface Identifiable<Value> {
    val id: Value

    fun equalsTo(other: Any?): Boolean {
        return this == other
    }
}

class IdentifiableDiffCallback<Type, Item : Identifiable<Type>> : DiffUtil.ItemCallback<Item>() {
    override fun areContentsTheSame(
        oldItem: Item,
        newItem: Item
    ): Boolean = oldItem.equalsTo(newItem)

    override fun areItemsTheSame(
        oldItem: Item,
        newItem: Item
    ): Boolean = oldItem.id == newItem.id

    companion object {
        fun <Item : Identifiable<Int>> int() = IdentifiableDiffCallback<Int, Item>()
        fun <Item : Identifiable<String>> string() = IdentifiableDiffCallback<String, Item>()
    }
}
