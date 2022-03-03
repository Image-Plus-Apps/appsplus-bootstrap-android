package uk.co.appsplus.bootstrap.mocks.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import uk.co.appsplus.bootstrap.mocks.models.MockAuthToken
import uk.co.appsplus.bootstrap.network.authenticator.TokenRefreshApi
import uk.co.appsplus.bootstrap.network.models.TokenRefresh

class MockTokenRefreshApi : TokenRefreshApi<MockAuthToken> {

    var tokenRefreshRequest: TokenRefresh? = null
    var authorizationRequest: String? = null
    var refreshError: Throwable? = null
    var tokens: MutableList<MockAuthToken>? = null
    var timeDelay: Long = 0

    override suspend fun createNewToken(
        tokenRefresh: TokenRefresh,
        authorization: String
    ): MockAuthToken {
        tokenRefreshRequest = tokenRefresh
        authorizationRequest = authorization
        return runBlocking {
            delay(timeDelay)
            refreshError?.let { throw it }
            tokens?.removeFirstOrNull() ?: throw Throwable("There was an error")
        }
    }
}
