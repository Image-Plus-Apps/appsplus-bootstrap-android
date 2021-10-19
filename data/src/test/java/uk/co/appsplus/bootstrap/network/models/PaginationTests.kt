package uk.co.appsplus.bootstrap.network.models

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PaginationTests : StringSpec() {

    init {
        "Test map" {
            val page = Pagination(
                listOf(1, 2, 3, 4, 5),
                Pagination.Meta(1, 3)
            )
            val newPage = page.map { it.toString() }
            newPage.items shouldBe listOf("1", "2", "3", "4", "5")
            newPage.meta.currentPage shouldBe 1
            newPage.meta.lastPage shouldBe 3
        }

        "Test mapNotNull" {
            val page = Pagination(
                listOf(1, 2, 3, 4, 5),
                Pagination.Meta(1, 3)
            )
            val newPage = page.mapNotNull {
                it.takeIf { it % 2 == 0 }?.toString()
            }
            newPage.items shouldBe listOf("2", "4")
            newPage.meta.currentPage shouldBe 1
            newPage.meta.lastPage shouldBe 3
        }

        "Test flatten" {
            val page = Pagination(
                listOf(
                    listOf(1),
                    listOf(2, 2),
                    listOf(3, 3, 3),
                    listOf(4, 4, 4, 4),
                    listOf(5, 5, 5, 5, 5)
                ),
                Pagination.Meta(1, 3)
            )
            val newPage = page.flatten()
            newPage.items shouldBe listOf(
                1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5
            )
            newPage.meta.currentPage shouldBe 1
            newPage.meta.lastPage shouldBe 3
        }

        "Test is last page" {
            Pagination(
                listOf(""),
                Pagination.Meta(2, 2)
            ).isLastPage shouldBe true

            Pagination(
                listOf(""),
                Pagination.Meta(3, 2)
            ).isLastPage shouldBe true

            Pagination(
                listOf(""),
                Pagination.Meta(1, 2)
            ).isLastPage shouldBe false
        }

        "Test has next page" {
            Pagination(
                listOf(""),
                Pagination.Meta(2, 2)
            ).hasNextPage shouldBe false

            Pagination(
                listOf(""),
                Pagination.Meta(1, 2)
            ).hasNextPage shouldBe true
        }
    }
}
