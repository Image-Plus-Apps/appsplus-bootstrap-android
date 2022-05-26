package uk.co.appsplus.bootstrap.network.headers

import okhttp3.Interceptor
import okhttp3.Response

class DeviceHeadersInterceptor(
    private val versionName: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain
            .request()
            .newBuilder()
            .addHeader("Device-Type", "android")
            .addHeader("Device-Version", versionName)
            .build()
            .run { chain.proceed(this) }
    }
}
