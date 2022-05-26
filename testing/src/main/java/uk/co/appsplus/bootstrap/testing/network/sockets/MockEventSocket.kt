package uk.co.appsplus.bootstrap.testing.network.sockets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.appsplus.bootstrap.network.models.sockets.SocketChannel
import uk.co.appsplus.bootstrap.network.models.sockets.SocketEvent
import uk.co.appsplus.bootstrap.network.models.sockets.SocketMessage

class MockEventSocket : EventSocket {

    var subscribedChannel: SocketChannel? = null
    var subscribedEvents: List<SocketEvent>? = null
    var subscriptionFlow: Flow<SocketMessage> = flow {}
    override fun subscribe(channel: SocketChannel, events: List<SocketEvent>): Flow<SocketMessage> {
        subscribedChannel = channel
        subscribedEvents = events
        return subscriptionFlow
    }
}
