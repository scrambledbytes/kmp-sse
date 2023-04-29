package cc.scrambledbytes.sse

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

// closed event streams should be re-connected
// failed event stream should not be re-connected


/**
 * Transforms the text stream
 */
class SseEventStream(
    private val onClose: () -> Unit, // closes the event stream
    private val onExecute: suspend (
        onState: (State) -> Unit,
        onLine: suspend (String) -> Unit,
    ) -> Unit, // executes the wrapped request
) {
    private val _events: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    internal val state = MutableStateFlow<State?>(null)
    val events: Flow<String>
        get() = _events

    private fun onState(newState: State) {
        state.value = newState
    }

    private suspend fun onLine(line: String) {
        _events.emit(line)
    }

    val isRetry: Boolean
        get() {
            val safeState = state.value ?: return false

            return safeState.isError && !isFailed
        }

    internal val isFailed: Boolean
        get() { // -> fail the connection
            val safeState = state.value

            return when {
                safeState == null -> false
                safeState.contentType != "text/event-stream" -> true
                safeState.isAborted -> true
                safeState.status != 200 -> true
                else -> false
            }
        }

    fun close() {
        onClose()
    }

    suspend fun connect() {
        onExecute(::onState, ::onLine)
    }

    data class State(
        val status: Int,
        val contentType: String,
        val isAborted: Boolean,
        val error: Throwable? = null,
    ) {
        val isError: Boolean by lazy {
            error != null
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