package cc.scrambledbytes.sse

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

suspend fun main() {
    val http = HttpClient {
        install(HttpTimeout) {
            socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
    }

    val provider: SseLineStream.Provider = KtorSseEventStreamProvider(http)

    val client = SseEventSource(
        url = "TODO",
        streamProvider = provider,
    )

    client.open()

    GlobalScope.launch {
        client.state.collectLatest {
            println("State: $it")
        }
    }

    client.events.collect {
        println("[Client] Got event: $it")
    }
}
