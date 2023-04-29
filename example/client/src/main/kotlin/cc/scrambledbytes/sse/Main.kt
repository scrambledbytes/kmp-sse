package cc.scrambledbytes.sse

import io.ktor.client.*

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val client = SseEventSourceImpl(
        url = "http://0.0.0.0:8080/sse",
        provider = provider,
    )

    client.open()

    client.events.collect {
        println("Got event: $it")
    }

    //client.close()
}