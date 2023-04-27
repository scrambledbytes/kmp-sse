package cc.scrambledbytes.sse

import io.ktor.client.*
import kotlinx.coroutines.*

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val job = Job()

    val client = SseClient(
        url = "http://0.0.0.0:8080/sse",
        withCredentials = false,
        provider = provider,
        context = job
    )

    client.connect()

    client.events.collect {
        println("Got event: $it")
    }
}