package uk.co.appsplus.bootstrap.network.models.sockets

sealed class SocketChannelError : Exception() {
    object Unknown : SocketChannelError()
    data class FailedToSubscribe(
        val channel: SocketChannel,
        val error: Exception
    ) : SocketChannelError()
}