package uk.co.appsplus.bootstrap.testing.arbs.ext

import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.of
import uk.co.appsplus.bootstrap.ui.pagination.PagingState

fun Arb.Companion.loadingState(
    onlyStates: List<PagingState>? = null,
    removedStates: List<PagingState> = emptyList()
): Arb<PagingState> {
    return onlyStates?.let {
        Arb.of(it)
    } ?: Arb.enum<PagingState>()
        .filterNot { it in removedStates }
}
