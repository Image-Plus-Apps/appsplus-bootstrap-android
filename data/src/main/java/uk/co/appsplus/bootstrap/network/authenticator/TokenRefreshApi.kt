package uk.co.appsplus.bootstrap.network.authenticator

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import uk.co.appsplus.bootstrap.network.models.AuthToken
import uk.co.appsplus.bootstrap.network.models.TokenRefresh

interface TokenRefreshApi<Token : AuthToken> {
    @POST("refresh")
    suspend fun refreshToken(
        @Body tokenRefresh: TokenRefresh,
        @Header("Authorization") authorization: String,
    ): Token
}
