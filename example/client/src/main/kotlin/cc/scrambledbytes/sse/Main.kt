package cc.scrambledbytes.sse

import io.ktor.client.*

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
