package uk.co.appsplus.bootstrap.network.sockets

import kotlinx.coroutines.flow.Flow
import uk.co.appsplus.bootstrap.network.models.sockets.SocketChannel
import uk.co.appsplus.bootstrap.network.models.sockets.SocketEvent
import uk.co.appsplus.bootstrap.network.models.sockets.SocketMessage

interface EventSocket {
    fun subscribe(channel: SocketChannel, events: List<SocketEvent>): Flow<SocketMessage>
}