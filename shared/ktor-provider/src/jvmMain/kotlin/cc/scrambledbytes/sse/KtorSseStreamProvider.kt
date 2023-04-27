package cc.scrambledbytes.sse

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.nio.charset.Charset

class KtorSseEventStreamProvider(
    private val client: HttpClient //TODO more generic ?
) : SseEventStream.Provider {

    override suspend fun create(url: String, lastEventId: String?): SseEventStream {
        // TODO parse url
        println("Connecting: $url, $lastEventId")

        var job: Job? = null

        val statement = client.prepareGet(url) {
            // TODO set headers
            headers {
                set(HttpHeaders.CacheControl, "no-store")
                // TODO set initiator type to "other"
                set(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
                lastEventId?.let { set("Last-Event-ID", it) }
            }

            job = executionContext
        }

        return SseEventStream(
            onClose = {
                println("Closing")
                job?.cancel()
            },
            onExecute = { onState, onLine ->
                statement.execute { response ->
                    onState(
                        SseEventStream.State(
                            status = response.status.value,
                            contentType = getContentType(response),
                            isAborted = false,
                            isError = !response.status.isSuccess()
                        )
                    )

                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead) {
                        var newBuffer: String? = null

                        channel.readAvailable {
                            newBuffer = it.decodeString()
                        }

                        newBuffer?.let {
                            val lines = it.split("\n")
                            for (line in lines) {
                                onLine(line)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun getContentType(response: HttpResponse): String =
        when (val type = response.contentType()) {
            null -> ""
            else -> "${type.contentType}/${type.contentSubtype}"
        }
}