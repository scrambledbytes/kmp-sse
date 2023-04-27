package cc.scrambledbytes.sse

import io.ktor.client.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.http.HttpClient

suspend fun main() {
    val http = HttpClient()
    val provider = KtorSseEventStreamProvider(http)

    val client = SseClient(
        url = "http://0.0.0.0:8080/sse",
        withCredentials = false,
        provider = provider,
        contextProvider = {
            GlobalScope.coroutineContext
        }
    )
    println("Started listening")


    client.connect()


    println("After connect")

    client.events.collect {
        println("Got event: $it")
    }
}