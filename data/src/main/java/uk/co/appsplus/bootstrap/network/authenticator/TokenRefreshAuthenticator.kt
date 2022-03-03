package uk.co.appsplus.bootstrap.network.authenticator

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.auth_session.currentToken
import uk.co.appsplus.bootstrap.network.models.AuthToken
import uk.co.appsplus.bootstrap.network.models.AuthorizationType
import uk.co.appsplus.bootstrap.network.models.TokenRefresh
import java.util.concurrent.CountDownLatch

class TokenRefreshAuthenticator<Token : AuthToken>(
    private val authSessionProvider: AuthSessionProvider,
    private val tokenRefreshApi: TokenRefreshApi<Token>
) : Authenticator {

    companion object {
        const val UNAUTHORIZED = 401
    }

    private var countDownLatch: CountDownLatch? = null

    override fun authenticate(route: Route?, response: Response): Request? {
        return if (response.request().tag(AuthorizationType::class.java) == AuthorizationType.PUBLIC ||
            response.code() != UNAUTHORIZED
        ) {
            null
        } else if (response.priorResponse()?.code() == UNAUTHORIZED) {
            authSessionProvider.replace(null)
            null
        } else {
            val freshAuthSession = fetchNewToken(
                authSessionProvider.currentToken(),
                authSessionProvider.deviceName()
            )

            freshAuthSession
                ?.let {
                    response
                        .request()
                        .newBuilder()
                        .header("Authorization", "Bearer ${it.accessToken}")
                        .build()
                }
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught", "SwallowedException")
    fun fetchNewToken(authSession: AuthToken?, deviceName: String): AuthToken? {
        authSession ?: return null
        synchronized(this) { countDownLatch?.await() }
            ?: run {
                countDownLatch = CountDownLatch(1)
                authSessionProvider.replace(
                    runBlocking {
                        try {
                            tokenRefreshApi.createNewToken(
                                TokenRefresh(deviceName),
                                "Bearer ${authSession.refreshToken}"
                            )
                        } catch (e: Throwable) {
                            null
                        }
                    }
                )
                countDownLatch?.countDown()
                countDownLatch = null
            }
        return authSessionProvider.currentToken()
    }
}
