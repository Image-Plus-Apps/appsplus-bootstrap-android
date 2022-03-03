package uk.co.appsplus.bootstrap.mocks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import uk.co.appsplus.bootstrap.mocks.models.MockAuthToken
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.models.AuthToken
import uk.co.appsplus.bootstrap.network.models.SimpleAuthToken

class MockAuthSessionProvider : AuthSessionProvider {
    var session: MockAuthToken? = null
    var replacedSession: MockAuthToken? = null
    var replacedSessionCalls = 0
    var deviceNameResponse: String = "device_name"
    var setDeviceName: String? = null
    var authSessionChangedFlow: Flow<MockAuthToken?>? = null

    override fun <Token : AuthToken> currentToken(clazz: Class<Token>): Token? {
        return if (clazz.toString() == SimpleAuthToken::class.java.toString()) {
            return session?.let {
                SimpleAuthToken(it.accessToken, it.refreshToken) as Token
            }
        } else {
            session
                ?.takeIf { it::class.java.toString() == clazz.toString() }
                ?.let { it as Token }
        }
    }

    override fun <Token : AuthToken> replace(authSession: Token?) {
        replacedSession = authSession as? MockAuthToken
        replacedSessionCalls += 1
    }

    override fun deviceName(): String {
        return deviceNameResponse
    }

    override fun setDeviceName(deviceName: String) {
        setDeviceName = deviceName
    }

    override fun authSessionChanged(): Flow<SimpleAuthToken?> {
        return authSessionChangedFlow?.map {
            it?.let {
                SimpleAuthToken(it.accessToken, it.refreshToken)
            }
        } ?: flow {
            emit(null)
        }
    }
}
