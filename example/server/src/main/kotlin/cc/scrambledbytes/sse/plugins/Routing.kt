package cc.scrambledbytes.sse.plugins

import cc.scrambledbytes.sse.debugTrace
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay

fun Application.configureRouting() {
    routing {
        get("/sse") {
            call.response.cacheControl(CacheControl.NoCache(null))

            try {
                debugTrace("Responding")
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    while (true) {
                        delay(3_000)

                        val event = RandomSseEvent()
                        debugTrace("Writing $event")
                        write("id: ${event.id}\n")
                        write("event: ${event.type}\n")
                        write("data: ${event.data}\n") // TODO multiline data
                        write("\n")
                        flush()
                    }
                }
            } catch (e: Exception) {
                debugTrace("Channel closed: ${e.message}")
            } finally {
                debugTrace("Shutting down channel")
            }
        }

        get("/sse-401") {
            call.respond(Unauthorized)
        }

        get("/sse-301") {
            call.respondRedirect("/sse", permanent = true)
        }

        get("/sse-500") {
            throw IllegalArgumentException("Test")
        }
    }
}

var count: Int = 0

fun RandomSseEvent(): SseEvent {
    return SseEvent(id = count++, type = "test", data = "data - ${System.currentTimeMillis()}")
}


data class SseEvent(
    val id: Int,
    val type: String,
    val data: String,
)