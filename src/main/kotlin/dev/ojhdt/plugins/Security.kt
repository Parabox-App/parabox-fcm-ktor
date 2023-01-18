package dev.ojhdt.plugins

import dev.ojhdt.data.model.MySession
import io.ktor.server.sessions.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.routing.*
import io.ktor.util.*

fun Application.configureSecurity() {

    install(Sessions) {
        cookie<MySession>("MY_SESSION")
    }

    intercept(Plugins) {
        if(call.sessions.get<MySession>() == null){
            call.sessions.set(MySession(generateNonce()))
        }
    }

}
