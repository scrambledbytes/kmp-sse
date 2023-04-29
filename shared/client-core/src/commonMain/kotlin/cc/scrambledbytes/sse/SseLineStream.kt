package cc.scrambledbytes.sse

import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// closed event streams should be re-connected
// failed event stream should not be re-connected


@JvmInline
value class SseLine(
    val value:String
) {
    // TODO validate UTF 8 encoding
}

/**
 * Wrapper at the HttpRequest that connects to the text stream
 *
 * The HttpRequest depends on the platform and engine used.
 */
class SseLineStream(
    private val onClose: () -> Unit, // cleanup hook
    // executes the wrapped request
    private val onExecute: suspend (
        onState: suspend (State) -> Unit,
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


    internal val state = MutableStateFlow<State?>(null)
    internal val lines: Flow<SseLine>
        get() = _lines

    private suspend fun onState(newState: State) {
        mutex.withLock {
            state.value = newState
        }
    }

    private suspend fun onLine(line: SseLine) {
        mutex.withLock {
            requireNotNull(state.value) { "No state, `onState` called before `onLine`?" }
            _lines.emit(line)
        }
    }

    fun close() {
        onClose()
    }

    suspend fun connect() {
        onExecute(::onState, ::onLine)
    }

    data class State(
        val statusCode: Int,
        val contentType: String,
        val isAborted: Boolean,
    ) {
        val isError: Boolean by lazy {
            statusCode != 200 // TODO handle 301 / 307
        }

        val isRetry: Boolean by lazy {
            isError && !isFailed
        }

        val isFailed: Boolean by lazy {
            when {
                //contentType != "text/event-stream" -> true
                isAborted -> true// provider thinks reconnection does not make sense
                statusCode == 204 -> true // in Protocol, see https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events-intro
                statusCode == 401 -> true // Unauthorized -> No point in reconnection
                statusCode == 403 -> true // Forbidden -> No point in reconnection
                else -> false
            }
        }
    }

    // TODO document
    fun interface Provider {
        /**
         * Let lastEventId be the EventSource object's last event ID string, encoded as UTF-8. Set (`Last-Event-ID`, lastEventIDValue) in request's header list.
         */
        suspend fun create(
            url: String,
            lastEventId: String?
        ): SseLineStream
    }
}