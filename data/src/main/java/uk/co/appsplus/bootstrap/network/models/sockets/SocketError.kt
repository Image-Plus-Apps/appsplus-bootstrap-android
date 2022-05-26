package uk.co.appsplus.bootstrap.network.models.sockets

import java.lang.Exception

sealed class SocketError : Exception() {
    object Unknown : SocketError()
}