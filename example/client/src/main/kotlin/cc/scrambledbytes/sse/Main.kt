package cc.scrambledbytes.sse

import io.ktor.client.*
import kotlinx.coroutines.*

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val client = SseClient(
        url = "http://0.0.0.0:8080/sse",
        provider = provider,
    )

    client.connect()

    client.disconnect()

    client.events.collect {
        println("Got event: $it")
    }
}