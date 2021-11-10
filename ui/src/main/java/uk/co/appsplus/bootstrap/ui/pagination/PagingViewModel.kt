package uk.co.appsplus.bootstrap.ui.pagination

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import uk.co.appsplus.bootstrap.network.models.Pagination

open class PagingViewModel<T> : ViewModel() {
    var nextPage = 1

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    class State<T> {
        val items = MutableStateFlow<List<T>>(listOf())
        val loadingState = MutableStateFlow(PagingState.InitialLoad)
        var hasNextPage = false
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val state = State<T>()

    val items: Flow<List<T>> get() = state.items
    val showItems = state.items.combine(state.loadingState) { items, loadingState ->
        items.isNotEmpty() &&
                loadingState !in listOf(PagingState.InitialLoad, PagingState.InitialLoadError)
    }

    val showLoading = state.loadingState.map { it == PagingState.InitialLoad }
    val showEmpty = state.items.combine(state.loadingState) { items, loadingState ->
        items.isEmpty() &&
                loadingState !in listOf(PagingState.InitialLoad, PagingState.InitialLoadError)
    }

    val failedToLoad = state.loadingState.map { it == PagingState.InitialLoadError }
    val pagingError = state.loadingState.map {
        when (it) {
            PagingState.RefreshingError -> PagingError.Refresh
            PagingState.PagingError -> PagingError.Paging
            else -> null
        }
    }

    val isRefreshing = state.loadingState.map { it == PagingState.Refreshing }
    val isPaging = state.loadingState.map { it == PagingState.Paging }

    suspend fun returnToIdle() {
        state.loadingState.emit(PagingState.Idle)
    }

    suspend fun refresh() {
        when (state.loadingState.value) {
            PagingState.InitialLoad, PagingState.Refreshing -> return
            else -> handleRefresh()
        }
    }

    suspend fun fetchMore() {
        if (!state.hasNextPage) return
        when (state.loadingState.value) {
            PagingState.Idle, PagingState.RefreshingError -> {
                handleFetchNextPage()
            }
            else -> return
        }
    }

    suspend fun retry() {
        when (state.loadingState.value) {
            PagingState.Idle, PagingState.InitialLoadError, PagingState.RefreshingError -> handleRefresh()
            PagingState.PagingError -> handleFetchNextPage()
            else -> return
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open suspend fun handleRefresh() {
        val loadingState = if (state.items.value.isNullOrEmpty()) {
            PagingState.InitialLoad
        } else {
            PagingState.Refreshing
        }
        state.loadingState.emit(loadingState)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open suspend fun handleFetchNextPage() {
        state.loadingState.emit(PagingState.Paging)
    }

    suspend fun handleLoading(loadingState: PagingState?) {
        when (loadingState) {
            PagingState.InitialLoad, PagingState.Refreshing -> {
                loadPage(page = 1)
            }
            PagingState.Paging -> {
                loadPage(nextPage)
            }
            else -> return
        }
    }

    open suspend fun loadPage(page: Int) {}

    suspend fun <Input> handleSuccess(
        pagination: Pagination<Input>,
        map: (List<Input>) -> List<T>
    ) {
        val (paginationItems, meta) = pagination

        nextPage = meta.currentPage + 1
        state.hasNextPage = meta.currentPage < meta.lastPage

        val items = (state.items.value).takeIf { meta.currentPage != 1 } ?: listOf()
        state.items.emit(items + map(paginationItems))

        state.loadingState.emit(PagingState.Idle)
    }

    suspend fun handleError() {
        val errorState = state.loadingState.value.errorState()
        state.loadingState.emit(errorState)
    }
}
