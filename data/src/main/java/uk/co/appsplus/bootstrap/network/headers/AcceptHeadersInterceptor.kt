package uk.co.appsplus.bootstrap.network.headers

import okhttp3.Interceptor
import okhttp3.Response

class AcceptHeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (chain.request().header("Accept") != null) return chain.proceed(chain.request())
        return chain
            .request()
            .newBuilder()
            .header("Accept", "application/json")
            .build()
            .run { chain.proceed(this) }
    }
}
