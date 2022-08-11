package uk.co.appsplus.bootstrap.testing.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.models.AuthToken

class MockAuthSessionProvider : AuthSessionProvider {
    var session: AuthToken? = null
    var replacedSession: AuthToken? = null
    var replacedSessionCalls = 0
    var deviceNameResponse: String = "device_name"
    var authSessionChangedFlow: Flow<AuthToken?>? = null

    override fun <Token : AuthToken> currentToken(clazz: Class<Token>): Token? {
        return session?.let { it as Token }
    }

    override fun deviceName(): String {
        return deviceNameResponse
    }

    override fun <Token : AuthToken> replace(authSession: Token?) {
        replacedSession = authSession
        replacedSessionCalls += 1
    }

    override fun setDeviceName(deviceName: String) {
        deviceNameResponse = deviceName
    }

    override fun authSessionChanged(): Flow<AuthToken?> {
        return authSessionChangedFlow ?: flow {
            emit(null)
        }
    }
}
