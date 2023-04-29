package cc.scrambledbytes.sse

import io.ktor.client.*

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val client = SseEventSource(
        url = "http://0.0.0.0:8080/sse",
        provider = provider,
    )

    client.connect()

    client.events.collect {
        println("Got event: $it")
    }
}