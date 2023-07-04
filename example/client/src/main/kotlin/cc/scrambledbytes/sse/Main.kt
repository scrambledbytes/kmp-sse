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
        url = "https://flysto-test.herokuapp.com/api/sse/klp40x/82",
        streamProvider = provider,
        extraHeaders = mapOf("Cookie" to "USER_SESSION=isServiceAccess%3D%2523bof%26userId%3D%2523sklp40x%2Fd45ad519d8f86c9d648557fb3218ecf2684a7847924514f2aa203ee2afa069b9")
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
