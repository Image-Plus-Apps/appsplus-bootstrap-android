package uk.co.appsplus.bootstrap.ui.pagination

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import uk.co.appsplus.bootstrap.ui.R

abstract class PagingFragment<T, VM, Adapter>(
    private val listAdapter: Adapter,
    private val pagingAdapter: PagingAdapter,
    private val pagingDirection: PagingDirection,
) : Fragment() where VM : PagingViewModel<T>, Adapter : ListAdapter<T, *> {

    enum class PagingDirection {
        TOP, BOTTOM
    }

    abstract val viewModel: VM
    val adapter = ConcatAdapter(listAdapter)
    private var currentSnackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureListAdapter(listAdapter)
        bindViewModel()
    }

    abstract fun hideRefreshing()

    protected open fun bindViewModel() {
        viewModel.items.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
        }
        viewModel.isRefreshing.observe(viewLifecycleOwner) {
            if (!it) {
                hideRefreshing()
            }
        }
        viewModel.isPaging.observe(viewLifecycleOwner) {
            if (it) {
                when (pagingDirection) {
                    PagingDirection.TOP -> adapter.addAdapter(0, pagingAdapter)
                    PagingDirection.BOTTOM -> adapter.addAdapter(pagingAdapter)
                }
            } else {
                adapter.removeAdapter(pagingAdapter)
            }
        }
        viewModel.pagingError.observe(viewLifecycleOwner) {
            hideCurrentSnackbar()
            currentSnackbar = when (it) {
                PagingError.Refresh -> {
                    createFetchErrorSnackbar()
                        ?.apply { addFailureSnackbarActions(this) }
                }
                PagingError.Paging -> {
                    createPagingFailedSnackbar()
                        ?.apply { addFailureSnackbarActions(this) }
                }
                else -> null
            }
            currentSnackbar?.show() ?: run { viewModel.returnToIdle() }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.state.loadingState.collect {
                viewModel.handleLoading(it)
            }
        }
    }

    private fun addFailureSnackbarActions(snackbar: Snackbar) {
        snackbar
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    currentSnackbar = null
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        viewModel.returnToIdle()
                    }
                }
            })
            .setAction(R.string.retry) {
                viewModel.retry()
            }
    }

    protected open fun configureListAdapter(adapter: Adapter) {
        // Empty implementation
    }

    abstract fun createFetchErrorSnackbar(): Snackbar?
    abstract fun createPagingFailedSnackbar(): Snackbar?

    protected fun hideCurrentSnackbar() {
        currentSnackbar?.dismiss()
    }
}
