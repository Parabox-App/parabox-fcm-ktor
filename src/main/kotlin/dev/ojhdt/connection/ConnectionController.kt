package dev.ojhdt.connection

import dev.ojhdt.data.model.User
import dev.ojhdt.exception.UserAlreadyExistsException
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ConnectionController(val fcmController: FcmController) {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()

    fun onConnect(sessionId: String, session: WebSocketSession) {
        if(connections.containsKey(sessionId)) {
            throw UserAlreadyExistsException()
        } else {
            connections.put(sessionId, session)
        }
    }

    fun onDisconnect(sessionId: String) {
        connections.remove(sessionId)
    }

    suspend fun sendToWS(sessionId: String, message: String) {
        if(connections.containsKey(sessionId)) {
            connections[sessionId]?.send(Frame.Text(message))
        }
    }

    fun sendToFCM(registrationToken: String, type: String, json: String) {
        fcmController.sendMessage(registrationToken, type, json)
    }
}