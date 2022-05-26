package uk.co.appsplus.bootstrap.mocks.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Tag
import uk.co.appsplus.bootstrap.network.models.AuthorizationType

interface MockApi {
    @GET("testauthorization")
    suspend fun testAuthorization(
        @Query("count") count: Int = 0,
        @Tag authorizationType: AuthorizationType?
    )

    @GET("testacceptheader")
    suspend fun testAcceptHeader(
        @Header("Accept") accept: String?
    )

    @GET("test")
    suspend fun test()
}
