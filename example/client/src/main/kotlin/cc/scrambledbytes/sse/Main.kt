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
        url = "http://0.0.0.0:8080/sse-301",
        streamProvider = provider,
        context = Job(), // execution context,
        withCredentials = false,
        delayProvider = { attempt ->
            (2 * attempt).seconds
        },
        isStreamFailed = { state ->
            state.statusCode == 406
        },
        isFatalError = {
            it is SocketTimeoutException
        },
        isConnectedProvider = {
            flow {
                emit(true)
            }
        }
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
