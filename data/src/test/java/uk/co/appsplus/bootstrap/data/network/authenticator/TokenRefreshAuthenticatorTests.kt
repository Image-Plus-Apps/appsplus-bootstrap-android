package uk.co.appsplus.bootstrap.data.network.authenticator

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.Arb
import io.kotest.property.checkAll
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.HttpException
import retrofit2.Retrofit
import uk.co.appsplus.bootstrap.mocks.data.MockApi
import uk.co.appsplus.bootstrap.mocks.data.MockAuthSessionProvider
import uk.co.appsplus.bootstrap.mocks.data.MockTokenRefreshApi
import uk.co.appsplus.bootstrap.mocks.models.MockAuthToken
import uk.co.appsplus.bootstrap.mocks.models.authTokenGenerator
import uk.co.appsplus.bootstrap.network.authenticator.TokenRefreshAuthenticator
import uk.co.appsplus.bootstrap.network.models.AuthorizationType
import java.lang.Exception
import java.util.concurrent.TimeUnit

class TokenRefreshAuthenticatorTests : StringSpec() {

    var _mockWebServer: MockWebServer? = null
    val mockWebServer get() = _mockWebServer!!

    var _client: OkHttpClient? = null
    val client: OkHttpClient get() = _client!!

    var _authSessionProvider: MockAuthSessionProvider? = null
    val authSessionProvider get() = _authSessionProvider!!

    var _mockTokenRefreshApi: MockTokenRefreshApi? = null
    val mockTokenRefreshApi get() = _mockTokenRefreshApi!!

    var _refreshAuthenticator: TokenRefreshAuthenticator<MockAuthToken>? = null
    val refreshAuthenticator get() = _refreshAuthenticator!!

    var _mockApi: MockApi? = null
    val mockApi get() = _mockApi!!

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        _mockWebServer = MockWebServer()
        _mockWebServer?.start()
        _mockTokenRefreshApi = MockTokenRefreshApi()
        _authSessionProvider = MockAuthSessionProvider()
        _refreshAuthenticator = TokenRefreshAuthenticator(
            authSessionProvider,
            mockTokenRefreshApi
        )

        _client = OkHttpClient
            .Builder()
            .authenticator(refreshAuthenticator)
            .build()

        _mockApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url(""))
            .client(client)
            .build()
            .create(MockApi::class.java)
    }

    override fun afterEach(testCase: TestCase, result: TestResult) {
        super.afterEach(testCase, result)
        _mockWebServer?.shutdown()
        _mockWebServer = null
        _authSessionProvider = null
        _refreshAuthenticator = null
        _mockApi = null
        _mockTokenRefreshApi = null
        _client = null
    }

    init {
        "If status is not 401 then do not refresh token" {
            checkAll(Arb.authTokenGenerator(), Arb.authTokenGenerator()) { oldAuthSession, newAuthSession ->
                mockWebServer.enqueue(MockResponse().setResponseCode(403))
                authSessionProvider.session = oldAuthSession
                mockTokenRefreshApi.tokens = mutableListOf(newAuthSession)
                try {
                    mockApi.testAuthorization(authorizationType = AuthorizationType.ACCESS_TOKEN)
                } catch (exception: HttpException) {
                    /* IGNORE */
                }
                mockTokenRefreshApi.authorizationRequest shouldBe null
                mockTokenRefreshApi.tokenRefreshRequest shouldBe null
                authSessionProvider.replacedSession shouldBe null
            }
        }

        "If request is public then do not refresh token" {
            checkAll(Arb.authTokenGenerator(), Arb.authTokenGenerator()) { oldAuthSession, newAuthSession ->
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                authSessionProvider.session = oldAuthSession
                mockTokenRefreshApi.tokens = mutableListOf(newAuthSession)
                try {
                    mockApi.testAuthorization(authorizationType = AuthorizationType.PUBLIC)
                } catch (exception: HttpException) {
                    /* IGNORE */
                }
                mockTokenRefreshApi.authorizationRequest shouldBe null
                mockTokenRefreshApi.tokenRefreshRequest shouldBe null
                authSessionProvider.replacedSession shouldBe null
            }
        }

        "If status is 401 then attempt to refresh token" {
            checkAll(Arb.authTokenGenerator(), Arb.authTokenGenerator()) { oldAuthSession, newAuthSession ->
                authSessionProvider.session = oldAuthSession
                mockTokenRefreshApi.tokens = mutableListOf(newAuthSession)
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                mockWebServer.enqueue(MockResponse().setResponseCode(200))
                try {
                    mockApi.testAuthorization(authorizationType = AuthorizationType.ACCESS_TOKEN)
                } catch (exception: HttpException) {
                    // IGNORE
                }
                mockTokenRefreshApi.authorizationRequest shouldBe "Bearer ${oldAuthSession.refreshToken}"
                mockTokenRefreshApi.tokenRefreshRequest?.deviceName shouldBe "device_name"
                authSessionProvider.replacedSession shouldBe newAuthSession
            }
        }

        "If second request returns 401 then do not try again and replace session with null" {
            checkAll(
                Arb.authTokenGenerator(),
                Arb.authTokenGenerator(),
                Arb.authTokenGenerator()
            ) { oldAuthSession, firstNewAuthSession, secondNewAuthSession ->
                authSessionProvider.session = oldAuthSession
                mockTokenRefreshApi.tokens = mutableListOf(firstNewAuthSession, secondNewAuthSession)
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                try {
                    mockApi.testAuthorization(authorizationType = AuthorizationType.ACCESS_TOKEN)
                } catch (exception: HttpException) {
                    // IGNORE
                }
                authSessionProvider.replacedSession shouldBe null
            }
        }

        "Retry request contains current auth session" {
            checkAll(
                Arb.authTokenGenerator(),
                Arb.authTokenGenerator(),
                Arb.authTokenGenerator()
            ) { currentAuthSession, firstNewAuthSession, secondNewAuthSession ->
                authSessionProvider.session = currentAuthSession
                mockTokenRefreshApi.tokens = mutableListOf(firstNewAuthSession, secondNewAuthSession)
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                mockWebServer.enqueue(MockResponse().setResponseCode(200))
                try {
                    mockApi.testAuthorization(authorizationType = AuthorizationType.ACCESS_TOKEN)
                } catch (exception: HttpException) {
                    // IGNORE
                }
                mockWebServer.takeRequest(1, TimeUnit.SECONDS)
                val succeededRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS)
                authSessionProvider.replacedSession shouldBe firstNewAuthSession
                succeededRequest?.getHeader("Authorization") shouldBe "Bearer ${currentAuthSession.accessToken}"
            }
        }

        "Multiple requests use only one token refresh" {
            authSessionProvider.session = MockAuthToken(
                "START_ACCESS_TOKEN",
                "START_REFRESH_TOKEN",
                1,
                "EXTRA"
            )
            mockTokenRefreshApi.tokens = mutableListOf(
                MockAuthToken("NEW_1_ACCESS_TOKEN", "NEW_1_REFRESH_TOKEN", 1, "EXTRA"),
                MockAuthToken("NEW_2_ACCESS_TOKEN", "NEW_2_REFRESH_TOKEN", 1, "EXTRA"),
                MockAuthToken("NEW_3_ACCESS_TOKEN", "NEW_3_REFRESH_TOKEN", 1, "EXTRA"),
            )
            mockTokenRefreshApi.timeDelay = 500
            mockWebServer.enqueue(MockResponse().setResponseCode(401))
            mockWebServer.enqueue(MockResponse().setResponseCode(401))
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            try {
                awaitAll(
                    async { mockApi.testAuthorization(1, AuthorizationType.ACCESS_TOKEN) },
                    async {
                        delay(50)
                        mockApi.testAuthorization(2, AuthorizationType.ACCESS_TOKEN)
                    }
                )
            } catch (exception: Exception) {
                // Ignore
            }

            mockWebServer.takeRequest(2, TimeUnit.SECONDS)
            mockWebServer.takeRequest(2, TimeUnit.SECONDS)
            val succeededRequest1 = mockWebServer.takeRequest(2, TimeUnit.SECONDS)
            val succeededRequest2 = mockWebServer.takeRequest(2, TimeUnit.SECONDS)

            authSessionProvider.replacedSessionCalls shouldBe 1
            authSessionProvider.replacedSession?.accessToken shouldBe "NEW_1_ACCESS_TOKEN"
            authSessionProvider.replacedSession?.refreshToken shouldBe "NEW_1_REFRESH_TOKEN"
            succeededRequest1?.getHeader("Authorization") shouldMatch Regex("Bearer .*")
            succeededRequest2?.getHeader("Authorization") shouldMatch Regex("Bearer .*")
        }
    }
}
