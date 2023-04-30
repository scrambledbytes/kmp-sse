package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.util.debugTrace
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentType.Text.EventStream
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.CacheControl
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

private val WITH_CREDENTIALS by lazy { AttributeKey<String>("withCredentials") }
private val INITIATOR by lazy { AttributeKey<String>("initiator") }
private val INCLUDE by lazy { "include" }
private val SAME_ORIGIN by lazy { "same-origin" }
private val NO_STORE by lazy { "no-store" }
private val LAST_EVENT_ID by lazy { "Last-Event-ID" }
private val OTHER by lazy { "other" }

class KtorSseEventStreamProvider(
    private val client: HttpClient
) : SseLineStream.Provider {

    override suspend fun create(
        url: SseUrl,
        lastEventId: String?,
        withCredentials: Boolean,
    ): SseLineStream {
        debugTrace("Connecting: $url, $lastEventId")

        var job: Job? = null

        val statement = client.prepareGet(url.value) {
            headers {
                set(CacheControl, NO_STORE)
                set(Accept, EventStream.toString())
                lastEventId?.let { set(LAST_EVENT_ID, it) }
            }

            setAttributes {
                put(INITIATOR, OTHER)
                put(WITH_CREDENTIALS, if (withCredentials) INCLUDE else SAME_ORIGIN)
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