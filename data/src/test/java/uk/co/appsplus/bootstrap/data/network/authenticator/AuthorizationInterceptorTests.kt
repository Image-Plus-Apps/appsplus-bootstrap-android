package uk.co.appsplus.bootstrap.data.network.authenticator

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import uk.co.appsplus.bootstrap.mocks.data.MockApi
import uk.co.appsplus.bootstrap.mocks.data.MockAuthSessionProvider
import uk.co.appsplus.bootstrap.mocks.models.authTokenGenerator
import uk.co.appsplus.bootstrap.network.authenticator.AuthorizationInterceptor
import uk.co.appsplus.bootstrap.network.models.AuthorizationType
import java.util.concurrent.TimeUnit

class AuthorizationInterceptorTests : StringSpec() {
    var _mockWebServer: MockWebServer? = null
    val mockWebServer get() = _mockWebServer!!

    var _client: OkHttpClient? = null
    val client get() = _client!!

    var _authSessionProvider: MockAuthSessionProvider? = null
    val authSessionProvider get() = _authSessionProvider!!

    var _authorizationInterceptor: AuthorizationInterceptor? = null
    val authorizationInterceptor get() = _authorizationInterceptor!!

    var _mockApi: MockApi? = null
    val mockApi get() = _mockApi!!

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        _mockWebServer = MockWebServer()
        _mockWebServer?.start()
        _authSessionProvider = MockAuthSessionProvider()
        _authorizationInterceptor = AuthorizationInterceptor(authSessionProvider)

        _client = OkHttpClient
            .Builder()
            .addInterceptor(authorizationInterceptor)
            .build()

        _mockApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url(""))
            .client(client)
            .build()
            .create(MockApi::class.java)
    }

    override fun afterEach(testCase: TestCase, result: TestResult) {
        super.afterEach(testCase, result)
        _client = null
        _mockWebServer?.shutdown()
        _mockWebServer = null
        _authSessionProvider = null
        _authorizationInterceptor = null
        _mockApi = null
    }

    init {
        "Setting authorization type to PUBLIC does not add auth session" {
            checkAll(Arb.authTokenGenerator().orNull()) {
                mockWebServer.enqueue(MockResponse().setResponseCode(200))
                authSessionProvider.session = it
                mockApi.testAuthorization(authorizationType = AuthorizationType.PUBLIC)
                val lastRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS)
                lastRequest?.getHeader("Authorization") shouldBe null
            }
        }

        "Setting authorization type to not PUBLIC sets bearer token on request" {
            val authorizationTypeGenerator = Arb.of(null, AuthorizationType.ACCESS_TOKEN)
            checkAll(Arb.authTokenGenerator(), authorizationTypeGenerator) { authSession, authorizationType ->
                mockWebServer.enqueue(MockResponse().setResponseCode(200))
                authSessionProvider.session = authSession
                mockApi.testAuthorization(authorizationType = authorizationType)
                val lastRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS)
                lastRequest?.getHeader("Authorization") shouldBe "Bearer ${authSession.accessToken}"
            }
        }

        "If auth session does not exist bearer is not set on request" {
            val authorizationTypeGenerator = Arb.of(
                null,
                AuthorizationType.ACCESS_TOKEN,
                AuthorizationType.PUBLIC
            )
            checkAll(authorizationTypeGenerator) {
                mockWebServer.enqueue(MockResponse().setResponseCode(200))
                authSessionProvider.session = null
                mockApi.testAuthorization(authorizationType = it)
                val lastRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS)
                lastRequest?.getHeader("Authorization") shouldBe null
            }
        }
    }
}
