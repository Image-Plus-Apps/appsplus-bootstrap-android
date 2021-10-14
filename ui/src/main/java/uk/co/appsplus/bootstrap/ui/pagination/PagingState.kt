package uk.co.appsplus.bootstrap.ui.pagination

enum class PagingState {
    Idle, InitialLoad, InitialLoadError, Refreshing, RefreshingError, Paging, PagingError
}

fun PagingState?.errorState(): PagingState {
    return when (this) {
        PagingState.InitialLoad, null -> PagingState.InitialLoadError
        PagingState.Refreshing -> PagingState.RefreshingError
        PagingState.Paging -> PagingState.PagingError
        else -> PagingState.RefreshingError
    }
}

enum class PagingError {
    Refresh, Paging
}
