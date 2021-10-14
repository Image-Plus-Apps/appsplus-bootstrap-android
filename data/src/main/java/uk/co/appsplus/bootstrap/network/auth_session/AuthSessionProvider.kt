package uk.co.appsplus.bootstrap.network.auth_session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.appsplus.bootstrap.network.models.AuthToken
import uk.co.appsplus.bootstrap.network.models.SimpleAuthToken

interface AuthSessionProvider {
    fun <Token : AuthToken> currentToken(clazz: Class<Token>): Token?
    fun <Token : AuthToken> replace(authSession: Token?)
    fun deviceName(): String
    fun authSessionChanged(): Flow<AuthToken?>
}

fun AuthSessionProvider.currentToken(): SimpleAuthToken? {
    return currentToken(SimpleAuthToken::class.java)
}

fun AuthSessionProvider.remove() {
    replace(null)
}

inline fun <reified Token : AuthToken> AuthSessionProvider.currentTokenFlow(): Flow<Token?> {
    return authSessionChanged().map { it as? Token }
}
