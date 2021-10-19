package uk.co.appsplus.bootstrap.network.headers

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import uk.co.appsplus.bootstrap.mocks.data.MockApi

class AcceptHeadersInterceptorTests : StringSpec() {

    var _mockWebServer: MockWebServer? = null
    val mockWebServer get() = _mockWebServer!!

    var _client: OkHttpClient? = null
    val client get() = _client

    var _mockApi: MockApi? = null
    val mockApi get() = _mockApi!!

    var _acceptHeadersInterceptor: AcceptHeadersInterceptor? = null
    val acceptHeadersInterceptor get() = _acceptHeadersInterceptor!!

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        _mockWebServer = MockWebServer()
        _mockWebServer?.start()

        _acceptHeadersInterceptor = AcceptHeadersInterceptor()

        _client = OkHttpClient
            .Builder()
            .addInterceptor(acceptHeadersInterceptor)
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
        _acceptHeadersInterceptor = null
        _mockApi = null
    }

    init {
        "Request accept header is set to application/json if null" {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            mockApi.testAcceptHeader(null)
            val request = mockWebServer.takeRequest()
            request.getHeader("Accept") shouldBe "application/json"
        }

        "Request accept header is unchanged if set" {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            mockApi.testAcceptHeader("text/html")
            val request = mockWebServer.takeRequest()
            request.getHeader("Accept") shouldBe "text/html"
        }
    }
}
