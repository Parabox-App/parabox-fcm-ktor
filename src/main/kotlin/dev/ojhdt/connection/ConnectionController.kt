package dev.ojhdt.connection

import com.google.gson.Gson
import dev.ojhdt.data.model.User
import dev.ojhdt.exception.UserAlreadyExistsException
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class ConnectionController() {
    private val logger = LoggerFactory.getLogger(ConnectionController::class.java)
    private val connections = ConcurrentHashMap<String, WebSocketSession>()

    @OptIn(DelicateCoroutinesApi::class)
    val fcmController: FcmController = FcmController() {
        GlobalScope.launch {
            sendToWS(it.session_id, it.message)
        }
    }

    fun onConnect(sessionId: String, session: WebSocketSession) {
        if (connections.containsKey(sessionId)) {
            throw UserAlreadyExistsException()
        } else {
            logger.info("User connected: $sessionId")
            connections.put(sessionId, session)
        }
    }

    fun onDisconnect(sessionId: String) {
        logger.info("User disconnected: $sessionId")
        connections.remove(sessionId)
    }

    suspend fun sendToWS(sessionId: String, message: String) {
        if (connections.containsKey(sessionId)) {
            val json = Gson().toJson(mapOf<String, String>(
                "data" to message,
                "type" to "server"
            ))
            connections[sessionId]?.send(Frame.Text(json))
        } else {
            logger.error("User not connected: $sessionId")
        }
    }

    fun sendToFCM(registrationToken: String, wsSessionId: String, type: String, json: String) {
        fcmController.sendMessage(registrationToken, wsSessionId, type, json)
    }
}