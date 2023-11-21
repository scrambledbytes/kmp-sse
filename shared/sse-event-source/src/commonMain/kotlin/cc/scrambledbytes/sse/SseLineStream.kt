package cc.scrambledbytes.sse

import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Wrapper at the HttpRequest that connects to the text stream
 *
 * The HttpRequest depends on the platform and engine used.
 */
class SseLineStream(
    private val onClose: () -> Unit, // cleanup hook
    // executes the wrapped request
    private val onConnect: suspend (
        onState: suspend (ConnectionState) -> Unit,
        onLine: suspend (SseLine) -> Unit,
    ) -> Unit,
) {
    private val mutex = Mutex()

    private val _lines: MutableSharedFlow<SseLine> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = SUSPEND,
    )

    internal suspend fun statusCode(): Int =
        mutex.withLock {
            state.value?.statusCode ?: -1
        }


    internal val state = MutableStateFlow<ConnectionState?>(null)
    internal val lines: Flow<SseLine>
        get() = _lines

    private suspend fun onConnected(
        newState: ConnectionState
    ) {
        mutex.withLock {
            require(state.value == null) { "Already connected, cannot call `onConnected` twice." }
            state.value = newState
        }
    }

    private suspend fun onLine(line: SseLine) {
        mutex.withLock {
            requireNotNull(state.value) { "No state, `onConnected` called before `onLine`?" }
            _lines.emit(line)
        }
    }

    fun close() {
        onClose()
    }

    suspend fun connect() {
        onConnect(::onConnected, ::onLine)
    }

    data class ConnectionState(
        val statusCode: Int,
        val contentType: String,
        val isAborted: Boolean,
    ) {
        private val isError: Boolean by lazy {
            statusCode != HTTP_OK
        }

        val isRetry: Boolean by lazy {
            isError && !isFailed
        }

        val isFailed: Boolean by lazy {
            when {
                //contentType != "text/event-stream" -> true
                isAborted -> true// provider thinks reconnection does not make sense
                statusCode == HTTP_NO_CONTENT -> true // in Protocol, see https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events-intro
                statusCode == HTTP_UNAUTHORIZED -> true // Unauthorized -> No point in reconnection
                statusCode == HTTP_FORBIDDEN -> true // Forbidden -> No point in reconnection
                else -> false
            }
        }
    }

    interface Provider {
        /**
         * Let lastEventId be the EventSource object's last event ID string, encoded as UTF-8.
         * Set (`Last-Event-ID`, lastEventIDValue) in request's header list.
         *
         * withCredentials: if true, set to include. Otherwise, use `same-origin`
         * https://fetch.spec.whatwg.org/#concept-request-credentials-mode
         */
        suspend fun create(
            url: SseUrl,
            lastEventId: String?,
            withCredentials: Boolean = false,
            extraHeaders: Map<String, String> = emptyMap(),
        ): SseLineStream

        fun parse(url: String): SseUrl
    }

    companion object {
        private const val HTTP_OK = 200
        private const val HTTP_NO_CONTENT = 204
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
    }
}
