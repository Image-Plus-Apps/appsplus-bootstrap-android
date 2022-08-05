package uk.co.appsplus.bootstrap.ui.pagination

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.co.appsplus.bootstrap.network.models.Pagination

open class PagingViewModel<T> : ViewModel() {

    enum class Direction {
        APPEND, PREPEND
    }

    var nextPage = 1

    class State<T> {
        val items = MutableStateFlow<List<T>>(listOf())
        val loadingState = MutableStateFlow(PagingState.InitialLoad)
        var hasNextPage = false
    }

    val state = State<T>()

    val items: Flow<List<T>> get() = state.items

    val showItems = state.items.combine(state.loadingState) { items, loadingState ->
        items.isNotEmpty() &&
                loadingState !in listOf(PagingState.InitialLoad, PagingState.InitialLoadError)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showLoading = state.loadingState.map { it == PagingState.InitialLoad }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val showEmpty = state.items.combine(state.loadingState) { items, loadingState ->
        items.isEmpty() &&
                loadingState !in listOf(PagingState.InitialLoad, PagingState.InitialLoadError)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val failedToLoad = state.loadingState.map { it == PagingState.InitialLoadError }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val pagingError = state.loadingState.map {
        when (it) {
            PagingState.RefreshingError -> PagingError.Refresh
            PagingState.PagingError -> PagingError.Paging
            else -> null
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isRefreshing = state.loadingState.map { it == PagingState.Refreshing }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isPaging = state.loadingState.map { it == PagingState.Paging }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    open suspend fun handleRefresh() {
        val loadingState = if (state.items.value.isEmpty()) {
            PagingState.InitialLoad
        } else {
            PagingState.Refreshing
        }
        state.loadingState.emit(loadingState)
    }

    open suspend fun handleFetchNextPage() {
        state.loadingState.emit(PagingState.Paging)
    }

    fun handleLoading(loadingState: PagingState?) {
        when (loadingState) {
            PagingState.InitialLoad, PagingState.Refreshing -> {
                viewModelScope.launch {
                    loadPage(page = 1)
                }
            }
            PagingState.Paging -> {
                viewModelScope.launch {
                    loadPage(nextPage)
                }
            }
            else -> return
        }
    }

    open suspend fun loadPage(page: Int) {}

    suspend fun <Input> handleSuccess(
        pagination: Pagination<Input>,
        map: (List<Input>) -> List<T>,
        direction: Direction = Direction.APPEND
    ) {
        val (paginationItems, meta) = pagination

        nextPage = meta.currentPage + 1
        state.hasNextPage = meta.currentPage < meta.lastPage

        val items = (state.items.value).takeIf { meta.currentPage != 1 } ?: listOf()
        when (direction) {
            Direction.APPEND -> {
                state.items.emit(items + map(paginationItems))
            }
            Direction.PREPEND -> {
                state.items.emit(map(paginationItems) + items)
            }
        }

        state.loadingState.emit(PagingState.Idle)
    }

    suspend fun handleError() {
        val errorState = state.loadingState.value.errorState()
        state.loadingState.emit(errorState)
    }

    fun updateItems(transform: (List<T>) -> List<T>) {
        viewModelScope.launch {
            state.items.emit(
                transform(state.items.value)
            )
        }
    }
}
