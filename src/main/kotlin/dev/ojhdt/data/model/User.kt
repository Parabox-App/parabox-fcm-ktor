package dev.ojhdt.data.model

import io.ktor.server.sessions.*
import io.ktor.websocket.*

data class User(
    val sessionId: String,
    val socket: WebSocketSession
)
