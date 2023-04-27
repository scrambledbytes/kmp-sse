package cc.scrambledbytes.sse

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

// closed event streams should be re-connected
// failed event stream should not be re-connected
class SseEventStream(
    private val onClose: () -> Unit, // closes the event stream
    private val onExecute: suspend (
            (State) -> Unit,
            suspend (String) -> Unit,
    ) -> Unit, // executes the wrapped request
) {
    private val _events: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    val state = MutableStateFlow<State?>(null)
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

    val isFailed: Boolean
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

    fun fireError() {
        //TODO onError("state: ${state.value}")
    }

    data class State(
        val status: Int,
        val contentType: String,
        val isAborted: Boolean,
        val isError: Boolean
    )

    // TODO document
    fun interface Provider {
        /**
         * Let lastEventId be the EventSource object's last event ID string, encoded as UTF-8. Set (`Last-Event-ID`, lastEventIDValue) in request's header list.
         */
        suspend fun create(url: String, lastEventId: String?): SseEventStream
    }
}