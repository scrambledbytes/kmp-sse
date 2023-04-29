package cc.scrambledbytes.sse

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

// closed event streams should be re-connected
// failed event stream should not be re-connected


/**
 * Wrapper at the HttpRequest that connects to the text stream
 *
 * The HttpRequest depends on the platform and engine used.
 */
class SseEventStream(
    private val onClose: () -> Unit, // cleanup hook
    // executes the wrapped request
    private val onExecute: suspend (
        onState: (State) -> Unit,
        onLine: suspend (String) -> Unit,
    ) -> Unit,
) {
    private val _events: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    internal val statusCode:Int
        get() = state.value?.statusCode ?: -1

    internal val state = MutableStateFlow<State?>(null)
    val events: Flow<String>
        get() = _events

    private fun onState(newState: State) {
        state.value = newState
    }

    private suspend fun onLine(line: String) {
        requireNotNull(state.value) {"No state, `onState` called before `onLine`?"}
        _events.emit(line)
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

        val isRetry:Boolean by lazy {
            isError && !isFailed
        }

        val isFailed:Boolean by lazy {
            when {
                contentType != "text/event-stream" -> true
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
        ): SseEventStream
    }
}