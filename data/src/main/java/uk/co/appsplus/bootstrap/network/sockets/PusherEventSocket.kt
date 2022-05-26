package uk.co.appsplus.bootstrap.network.sockets

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.auth_session.currentToken
import uk.co.appsplus.bootstrap.network.models.sockets.SocketChannel
import uk.co.appsplus.bootstrap.network.models.sockets.SocketEvent
import uk.co.appsplus.bootstrap.network.models.sockets.SocketMessage
import java.lang.Exception
import java.util.concurrent.CancellationException

class PusherEventSocket(
    private val apiKey: String,
    private val options: PusherOptions,
    authenticationUrl: String,
    private val authSessionProvider: AuthSessionProvider,
    private val additionalAuthHeaders: Map<String, String>,
) : EventSocket {

    private var pusher: Pusher? = null
    private val subscriptions = mutableMapOf<SocketChannel, SharedFlow<SocketMessage>>()
    private val eventsFlow = MutableSharedFlow<SocketMessage>()
    private val authorizer = HttpAuthorizer(authenticationUrl)

    private fun initialisePusher(): Pusher {
        authorizer.setHeaders(
            mapOf(
                "Authorization" to authSessionProvider.currentToken()?.accessToken
            ).plus(additionalAuthHeaders)
        )
        options.authorizer = authorizer
        pusher = Pusher(
            apiKey,
            options
        )
        return pusher!!
    }

    private fun attemptConnection() : Flow<Pusher> {
        val pusher = this.pusher
        return if (pusher != null && pusher.connection.state == ConnectionState.CONNECTED) {
            flow {
                emit(pusher)
            }
        } else {
            callbackFlow {
                val newPusher = initialisePusher()
                this@PusherEventSocket.pusher = newPusher
                newPusher.connect(
                    object : ConnectionEventListener {
                        override fun onConnectionStateChange(change: ConnectionStateChange?) {
                            trySend(newPusher)
                            channel.close()
                        }

                        override fun onError(message: String?, code: String?, e: Exception?) {
                            cancel(CancellationException(message))
                        }
                    },
                    ConnectionState.CONNECTED
                )
            }
        }
    }

    private fun unsubscribeFromChannel(channel: SocketChannel) {
        pusher?.unsubscribe(channel.name)
    }

    override fun subscribe(channel: SocketChannel, events: List<SocketEvent>): Flow<SocketMessage> {
        val filter: (SocketMessage) -> Boolean = {
            val correctEvent = if (events.isEmpty()) {
                true
            } else {
                (events + listOf(SocketEvent.CONNECTED, SocketEvent.DISCONNECTED, SocketEvent.SUBSCRIBED))
                    .contains(it.event)
            }
            it.channel == channel && correctEvent
        }

        return subscriptions[channel]?.filter { filter(it) } ?: run {
            val scope = CoroutineScope(Dispatchers.IO)
            val flow = eventsFlow
                .onStart {
                    attemptConnection()
                        .map {
                            if (channel.isPrivate) {
                                it.subscribePrivate(channel.name)
                            } else {
                                it.subscribe(channel.name)
                            }
                        }
                        .collect()
                }
                .catch {
                    unsubscribeFromChannel(channel)
                }
                .shareIn(scope, SharingStarted.WhileSubscribed())
            subscriptions[channel] = flow

            flow.filter { filter(it) }
        }
    }
}