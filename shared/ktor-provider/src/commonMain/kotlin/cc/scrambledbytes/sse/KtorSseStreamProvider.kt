package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.util.debugTrace
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*


class KtorSseEventStreamProvider(
    private val client: HttpClient
) : SseLineStream.Provider {

    override suspend fun create(
        url: SseUrl,
        lastEventId: String?
    ): SseLineStream {
        debugTrace("Connecting: $url, $lastEventId")

        var job: Job? = null

        val statement = client.prepareGet(url.value) {
            // TODO set headers
            headers {
                set(HttpHeaders.CacheControl, "no-store")
                // TODO set initiator type to "other"
                set(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
                lastEventId?.let { set("Last-Event-ID", it) }
            }

            job = executionContext
        }

        return SseLineStream(
            onClose = {
                debugTrace("Closing SSE event stream")
                job?.cancel()
            },
            onExecute = { onState, onLine ->
                statement.execute { response ->
                    debugTrace("Got response: $response")
                    onState(
                        SseLineStream.ConnectionState(
                            statusCode = response.status.value,
                            contentType = getContentType(response),
                            isAborted = response.status == HttpStatusCode.NoContent,
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
                                onLine(SseLine(line))
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

    override fun parse(url: String): SseUrl {
        Url(url)
        return SseUrl(url)
    }
}