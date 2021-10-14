package uk.co.appsplus.bootstrap.network.auth_session

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.appsplus.bootstrap.data.local_storage.secure.KeyValueStorage
import uk.co.appsplus.bootstrap.network.models.AuthToken
import uk.co.appsplus.bootstrap.network.models.SimpleAuthToken
import java.util.*

class AuthSessionProviderImpl(
    private val storage: KeyValueStorage
) : AuthSessionProvider {

    companion object {
        private const val AUTH_TOKEN = "auth_token"
        private const val DEVICE_NAME = "device_name"
    }

    private val authSessionChannel = MutableSharedFlow<AuthToken?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun <Token : AuthToken> currentToken(clazz: Class<Token>): Token? {
        return storage.getItem(AUTH_TOKEN, clazz)
    }

    override fun <Token : AuthToken> replace(authSession: Token?) {
        authSession?.let { storage.putItem(AUTH_TOKEN, it) } ?: storage.removeItem(AUTH_TOKEN)
        authSessionChannel.tryEmit(authSession)
    }

    override fun deviceName(): String {
        return storage.getString(DEVICE_NAME) ?: storage.run {
            val uuid = UUID.randomUUID().toString()
            putItem(DEVICE_NAME, uuid)
            uuid
        }
    }

    override fun authSessionChanged(): Flow<AuthToken?> {
        return authSessionChannel
    }
}
