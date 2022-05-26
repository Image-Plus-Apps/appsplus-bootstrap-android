package uk.co.appsplus.bootstrap.network.sockets

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import uk.co.appsplus.bootstrap.network.auth_session.AuthSessionProvider
import uk.co.appsplus.bootstrap.network.auth_session.currentToken
import uk.co.appsplus.bootstrap.network.models.sockets.SocketChannel
import uk.co.appsplus.bootstrap.network.models.sockets.SocketEvent
import uk.co.appsplus.bootstrap.network.models.sockets.SocketMessage
import java.lang.Exception
import java.util.concurrent.CancellationException

@ExperimentalCoroutinesApi
@FlowPreview
class PusherEventSocket(
    apiKey: String,
    options: PusherOptions,
    authenticationUrl: String,
    private val authSessionProvider: AuthSessionProvider,
    private val additionalAuthHeaders: Map<String, String>,
) : EventSocket {

    private val authorizer = HttpAuthorizer(authenticationUrl)
    private val pusher = Pusher(
        apiKey,
        options.setAuthorizer(
            authorizer
        )
    )
    private val subscriptions = mutableMapOf<SocketChannel, SharedFlow<SocketMessage>>()

    private fun initialisePusher() {
        authorizer.setHeaders(
            mapOf(
                "Authorization" to "Bearer ${authSessionProvider.currentToken()?.accessToken ?: ""}"
            ).plus(additionalAuthHeaders)
        )
    }

    private fun attemptConnection(): Flow<Pusher> {
        return if (pusher.connection.state == ConnectionState.CONNECTED) {
            flow {
                emit(pusher)
            }
        } else {
            callbackFlow {
                initialisePusher()
                pusher.connect(
                    object : ConnectionEventListener {
                        override fun onConnectionStateChange(change: ConnectionStateChange?) {
                            trySend(pusher)
                            channel.close()
                        }

                        override fun onError(message: String?, code: String?, e: Exception?) {
                            cancel(CancellationException(message))
                        }
                    },
                    ConnectionState.CONNECTED
                )

                awaitClose {}
            }
        }
    }

    private fun unsubscribeFromChannel(channel: SocketChannel) {
        pusher.unsubscribe(channel.name)
        subscriptions.remove(channel)
    }

    override fun subscribe(
        channel: SocketChannel,
        events: List<SocketEvent>
    ): Flow<SocketMessage> {
        val filter: (SocketMessage) -> Boolean = {
            val correctEvent = if (events.isEmpty()) {
                true
            } else {
                it.event in events + listOf(
                    SocketEvent.CONNECTED,
                    SocketEvent.DISCONNECTED,
                    SocketEvent.SUBSCRIBED
                )
            }
            it.channel == channel && correctEvent
        }

        return subscriptions[channel]?.filter { filter(it) } ?: run {
            val scope = CoroutineScope(Dispatchers.IO)
            val flow = attemptConnection()
                .flatMapLatest {
                    callbackFlow {
                        val eventListener = createEventListener(this)
                        it.subscribePrivate(
                            channel.name,
                            eventListener
                        ).also {
                            it.bindGlobal(eventListener)
                        }

                        awaitClose {
                            unsubscribeFromChannel(channel)
                        }
                    }
                }
                .catch {
                    unsubscribeFromChannel(channel)
                }
                .shareIn(scope, SharingStarted.WhileSubscribed())

            subscriptions[channel] = flow

            flow.filter { filter(it) }
        }
    }

    private fun createEventListener(
        scope: ProducerScope<SocketMessage>
    ): PrivateChannelEventListener {
        return object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                scope.trySend(
                    SocketMessage(
                        SocketChannel(event?.channelName ?: ""),
                        SocketEvent(event?.eventName ?: ""),
                        event?.data
                    )
                )
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                // Ignore
            }

            override fun onAuthenticationFailure(
                message: String?,
                e: Exception?
            ) {
                scope.cancel(CancellationException(message))
            }
        }
    }
}
