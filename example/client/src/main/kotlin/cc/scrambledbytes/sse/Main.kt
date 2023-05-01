package cc.scrambledbytes.sse

import io.ktor.client.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val http = HttpClient()
    val provider: SseLineStream.Provider = KtorSseEventStreamProvider(http)

    val client = SseEventSource(
        url = "http://0.0.0.0:8080/sse",
        streamProvider = provider
    )

    client.open()

    client.events.collect {
        println("[Client] Got event: $it")
    }
}
