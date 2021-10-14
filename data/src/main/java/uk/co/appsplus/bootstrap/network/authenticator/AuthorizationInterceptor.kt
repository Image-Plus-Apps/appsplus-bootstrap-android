package uk.co.appsplus.bootstrap.network.authenticator

import okhttp3.Interceptor
import okhttp3.Response
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.auth_session.currentToken
import uk.co.appsplus.bootstrap.network.models.AuthorizationType

class AuthorizationInterceptor(
    private val authSessionProvider: AuthSessionProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authSession = authSessionProvider.currentToken() ?: return chain.proceed(chain.request())
        return chain
            .request()
            .takeUnless { it.tag(AuthorizationType::class.java) == AuthorizationType.PUBLIC }
            ?.newBuilder()
            ?.header("Authorization", "Bearer ${authSession.accessToken}")
            ?.let { chain.proceed(it.build()) }
            ?: chain.proceed(chain.request())
    }
}
