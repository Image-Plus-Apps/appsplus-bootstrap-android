package uk.co.appsplus.bootstrap.network.sockets

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.*
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
        if (subscriptions.isEmpty()) {
            pusher.disconnect()
        }
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
                        when {
                            channel.isPresence() -> {
                                val eventListener = SimpleEventListener.presence(this)
                                it.subscribePresence(
                                    channel.name,
                                    eventListener
                                ).bindGlobal(eventListener)
                            }
                            channel.isPrivate() -> {
                                val eventListener = SimpleEventListener.private(this)
                                it.subscribePrivate(
                                    channel.name,
                                    eventListener
                                ).bindGlobal(eventListener)
                            }
                            else -> {
                                val eventListener = SimpleEventListener.private(this)
                                it.subscribe(
                                    channel.name,
                                    eventListener
                                ).bindGlobal(eventListener)
                            }
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

    private fun SocketChannel.isPrivate(): Boolean {
        return name.startsWith("private-")
    }

    private fun SocketChannel.isPresence(): Boolean {
        return name.startsWith("presence-")
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
private open class SimpleEventListener(
    val scope: ProducerScope<SocketMessage>
): ChannelEventListener {

    companion object {
        fun default(scope: ProducerScope<SocketMessage>): ChannelEventListener {
            return SimpleEventListener(scope)
        }

        fun private(scope: ProducerScope<SocketMessage>): PrivateChannelEventListener {
            return object : SimpleEventListener(scope), PrivateChannelEventListener {
                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    scope.cancel(CancellationException(message))
                }
            }
        }

        fun presence(scope: ProducerScope<SocketMessage>): PresenceChannelEventListener {
            return object : SimpleEventListener(scope), PresenceChannelEventListener {
                override fun onAuthenticationFailure(message: String?, e: Exception?) {
                    scope.cancel(CancellationException(message))
                }

                override fun onUsersInformationReceived(
                    channelName: String?,
                    users: MutableSet<User>?
                ) {
                    // Ignore
                }

                override fun userSubscribed(channelName: String?, user: User?) {
                    // Ignore
                }

                override fun userUnsubscribed(channelName: String?, user: User?) {
                    // Ignore
                }
            }
        }
    }

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
}
