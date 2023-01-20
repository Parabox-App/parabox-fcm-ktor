package dev.ojhdt.plugins

import dev.ojhdt.connection.ConnectionController
import dev.ojhdt.route.register
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        val connectionController by inject<ConnectionController>()
        register(connectionController)
    }
}
