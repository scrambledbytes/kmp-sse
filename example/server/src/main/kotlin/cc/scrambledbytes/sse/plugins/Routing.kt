package cc.scrambledbytes.sse.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import kotlinx.coroutines.delay

fun Application.configureRouting() {
    routing {
        get("/sse") {
            call.response.cacheControl(CacheControl.NoCache(null))

            try {
                println("Responding")
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    while(true) {
                        delay(3_000)

                        val event = RandomSseEvent()
                        println("Writing $event")
                        write("event: ${event.type}\n")
                        write("data: ${event.data}\n") // TODO multiline data
                        write("")
                        flush()
                    }
                }
            }
            catch (e:Exception) {
                println("Channel closed: ${e.message}")
            }
            finally {
                println("Shutting down channel")
            }

        }
    }
}

fun RandomSseEvent():SseEvent {
    return SseEvent(type = "test", data="data - ${System.currentTimeMillis()}")
}

data class SseEvent(
    val type: String,
    val data: String,
)