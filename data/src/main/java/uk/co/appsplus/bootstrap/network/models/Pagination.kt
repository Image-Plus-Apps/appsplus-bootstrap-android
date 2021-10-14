package uk.co.appsplus.bootstrap.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Pagination<T>(
    @Json(name = "data")
    val items: List<T>,
    @Json(name = "meta")
    val meta: Meta,
) : Serializable {

    @JsonClass(generateAdapter = true)
    data class Meta(
        @Json(name = "current_page")
        val currentPage: Int,
        @Json(name = "last_page")
        val lastPage: Int,
    ) : Serializable

    val isLastPage: Boolean get() = meta.currentPage >= meta.lastPage

    val hasNextPage: Boolean get() = meta.currentPage < meta.lastPage

    fun <S> map(transform: (T) -> S): Pagination<S> {
        return Pagination(
            items.map(transform),
            meta
        )
    }

    fun <S> mapNotNull(transform: (T) -> S?): Pagination<S> {
        return Pagination(
            items.mapNotNull(transform),
            meta
        )
    }
}

fun <N> Pagination<List<N>>.flatten(): Pagination<N> {
    return Pagination(
        items.flatten(),
        meta
    )
}
