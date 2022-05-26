package uk.co.appsplus.bootstrap.network.models.sockets

data class SocketEvent(val name: String) {
    companion object {
        val SUBSCRIBED = SocketEvent("Subscribed")
        val CONNECTED = SocketEvent("Connected")
        val DISCONNECTED = SocketEvent("Disconnected")
    }
}