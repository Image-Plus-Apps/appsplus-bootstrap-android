package uk.co.appsplus.bootstrap.ui.layout_manager

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PercentageLinearLayoutManager(
    context: Context,
    @RecyclerView.Orientation
    orientation: Int = RecyclerView.HORIZONTAL,
    reverseLayout: Boolean = false,
    private val percentage: Float = 1f,
    private val extra: Int = 0,
) : LinearLayoutManager(context, orientation, reverseLayout) {
    override fun generateDefaultLayoutParams() =
        scaledLayoutParams(super.generateDefaultLayoutParams())

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?) =
        scaledLayoutParams(super.generateLayoutParams(lp))

    override fun generateLayoutParams(c: Context?, attrs: AttributeSet?) =
        scaledLayoutParams(super.generateLayoutParams(c, attrs))

    private fun scaledLayoutParams(layoutParams: RecyclerView.LayoutParams) =
        layoutParams.apply {
            when (orientation) {
                RecyclerView.HORIZONTAL -> {
                    width = (horizontalSpace * percentage).toInt() - extra
                }
                RecyclerView.VERTICAL -> {
                    height = (verticalSpace * percentage).toInt() - extra
                }
            }
        }

    private val horizontalSpace get() = width - paddingStart - paddingEnd
    private val verticalSpace get() = height - paddingTop - paddingBottom
}
