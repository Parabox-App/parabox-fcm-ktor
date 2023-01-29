package dev.ojhdt.plugins

import com.google.gson.JsonParser
import dev.ojhdt.connection.ConnectionController
import dev.ojhdt.route.register
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Application.configureRouting() {
    routing {
        val logger = LoggerFactory.getLogger(ConnectionController::class.java)
        val connectionController by inject<ConnectionController>()
        register(connectionController)
        get("/") {
            call.respond(mapOf("version" to "1.0.0"))
        }
        post("/receive/") {
            val text = call.receiveText()
            val jsonObj = JsonParser.parseString(text).asJsonObject
            val tokens = jsonObj.getAsJsonArray("targetTokensSet")
            val json = jsonObj.get("receiveMessageDto").toString()
            tokens.forEach {token ->
                connectionController.fcmController.sendMessage(
                    registrationToken = token.asString,
                    wsSessionId = "",
                    type = "receive",
                    json = json
                )
                logger.info("Received: ${token.asString}")
            }
            call.respond(HttpStatusCode.OK)
        }
        post("/send/") {
            val text = call.receiveText()
            val jsonObj = JsonParser.parseString(text).asJsonObject
            val token = jsonObj.get("loopbackToken").asString
            val json = jsonObj.get("sendMessageDto").toString()
            connectionController.fcmController.sendMessage(
                registrationToken = token,
                wsSessionId = "",
                type = "send",
                json = json
            )
            logger.info("Sent: $token")
            call.respond(HttpStatusCode.OK)
        }
    }
}
