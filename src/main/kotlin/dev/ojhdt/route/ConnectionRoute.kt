package dev.ojhdt.route

import dev.ojhdt.connection.ConnectionController
import dev.ojhdt.data.model.MySession
import dev.ojhdt.data.model.User
import dev.ojhdt.exception.UserAlreadyExistsException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.register(connectionController: ConnectionController) {
    webSocket("/ws") {
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
                if(it is Frame.Text) {
                    print(it.readText())
                }
            }
        } catch (e: UserAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connectionController.onDisconnect(session.sessionId)
        }
    }
}