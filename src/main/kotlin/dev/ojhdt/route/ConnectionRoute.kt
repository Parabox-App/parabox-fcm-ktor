package dev.ojhdt.route

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.ojhdt.connection.ConnectionController
import dev.ojhdt.data.model.DownstreamMessage
import dev.ojhdt.data.model.MySession
import dev.ojhdt.exception.UserAlreadyExistsException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

fun Route.register(connectionController: ConnectionController) {
    webSocket("/ws") {
        val logger = LoggerFactory.getLogger(ConnectionController::class.java)
        val session = call.sessions.get<MySession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return@webSocket
        }
        try {
            connectionController.onConnect(
                sessionId = session.sessionId, session = this
            )
            incoming.consumeEach {
                if (it is Frame.Text) {
                    // SLF4J log the text
                    logger.info("Received: ${it.readText()}")
                    val downStreamMessage = Gson().fromJson(it.readText(), DownstreamMessage::class.java)
                    connectionController.sendToFCM(downStreamMessage.token, session.sessionId, downStreamMessage.type, downStreamMessage.data)
                }
            }
        } catch (e: UserAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: JsonSyntaxException){
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connectionController.onDisconnect(session.sessionId)
        }
    }
}