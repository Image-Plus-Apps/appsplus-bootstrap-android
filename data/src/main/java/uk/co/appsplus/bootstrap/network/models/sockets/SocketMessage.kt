package uk.co.appsplus.bootstrap.network.models.sockets

data class SocketMessage(
    val channel: SocketChannel?,
    val event: SocketEvent,
    val data: String?
)