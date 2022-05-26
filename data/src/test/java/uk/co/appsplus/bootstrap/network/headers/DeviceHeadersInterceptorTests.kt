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

class DeviceHeadersInterceptorTests : StringSpec() {

    private var _mockWebServer: MockWebServer? = null
    private val mockWebServer get() = _mockWebServer!!

    private var _client: OkHttpClient? = null
    private val client get() = _client

    private var _mockApi: MockApi? = null
    private val mockApi get() = _mockApi!!

    private var _deviceHeadersInterceptor: DeviceHeadersInterceptor? = null
    private val deviceHeadersInterceptor get() = _deviceHeadersInterceptor!!

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        _mockWebServer = MockWebServer()
        _mockWebServer?.start()

        _deviceHeadersInterceptor = DeviceHeadersInterceptor("VERSION")

        _client = OkHttpClient
            .Builder()
            .addInterceptor(deviceHeadersInterceptor)
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
        _deviceHeadersInterceptor = null
        _mockApi = null
    }

    init {
        "Request device type header is set to android" {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            mockApi.test()
            val request = mockWebServer.takeRequest()
            request.getHeader("Device-Type") shouldBe "android"
        }

        "Request device version header is set to VERSION" {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
            mockApi.test()
            val request = mockWebServer.takeRequest()
            request.getHeader("Device-Version") shouldBe "VERSION"
        }
    }
}