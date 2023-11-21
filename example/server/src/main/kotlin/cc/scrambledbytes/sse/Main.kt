package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}

fun Any.debugTrace(message: String) {
    println("[${this::class.simpleName}] $message")
}
