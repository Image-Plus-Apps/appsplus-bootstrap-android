package uk.co.appsplus.bootstrap.network.models.sockets

data class SocketChannel(val name: String) {
    internal val isPrivate = name.startsWith("private-")
}