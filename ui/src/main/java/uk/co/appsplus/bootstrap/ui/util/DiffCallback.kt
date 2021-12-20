package uk.co.appsplus.bootstrap.ui.util

import androidx.recyclerview.widget.DiffUtil
import kotlin.reflect.KProperty1

class DiffCallback<Item>(
    private val identityProperty: (Item, Item) -> Boolean,
    private val equalityFunction: (Item, Item) -> Boolean
) : DiffUtil.ItemCallback<Item>() {

    companion object {
        fun <Item, Value> create(identityProperty: KProperty1<Item, Value>): DiffCallback<Item> {
            return DiffCallback(
                { lhs, rhs -> identityProperty.get(lhs) == identityProperty.get(rhs) },
                { lhs, rhs -> lhs == rhs }
            )
        }

        fun <Item> create(): DiffCallback<Item> {
            return DiffCallback(
                { lhs, rhs -> lhs == rhs },
                { lhs, rhs -> lhs == rhs }
            )
        }
    }

    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return identityProperty(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return equalityFunction(oldItem, newItem)
    }
}
