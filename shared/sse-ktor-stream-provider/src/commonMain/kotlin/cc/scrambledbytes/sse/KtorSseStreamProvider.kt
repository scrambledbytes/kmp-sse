package cc.scrambledbytes.sse

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Text.EventStream
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Job

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
        extraHeaders: Map<String, String>
    ): SseLineStream {
        var job: Job? = null

        val statement = client.prepareGet(url.value) {
            headers {
                set(CacheControl, NO_STORE)
                set(Accept, EventStream.toString())
                lastEventId?.let { set(LAST_EVENT_ID, it) }

                extraHeaders.forEach { (name, value) ->
                    set(name, value)
                }
            }

            setAttributes {
                put(INITIATOR, OTHER)
                put(WITH_CREDENTIALS, if (withCredentials) INCLUDE else SAME_ORIGIN)
            }

            job = executionContext
        }

        return SseLineStream(
            onClose = {
                job?.cancel()
            },
            onConnect = { onConnected, onLine ->
                statement.execute { response ->
                    onConnected(
                        SseLineStream.ConnectionState(
                            statusCode = response.status.value,
                            contentType = getContentType(response),
                            isAborted = response.status == HttpStatusCode.NoContent,
                        )
                    )

                    val channel: ByteReadChannel = response.body()

                    var line: String?
                    do {
                        line = channel.readUTF8Line()
                        if (line != null) {
                            onLine(SseLine(line))
                        }
                    } while (line != null)
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
