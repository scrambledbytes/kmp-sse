package cc.scrambledbytes.sse

import io.ktor.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val client = SseEventSourceImpl(
        url = "http://0.0.0.0:8080/sse-401",
        provider = provider,
    )

    client.open()

    GlobalScope.launch {

        client.state.collect {
            println("[Client] Got state: $it")
        }

    }

    client.events.collect {
        println("[Client] Got event: $it")
    }

    //client.close()
}
