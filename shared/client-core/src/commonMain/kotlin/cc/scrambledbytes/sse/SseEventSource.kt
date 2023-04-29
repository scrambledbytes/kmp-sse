package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.impl.tryConnect
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal const val LF = '\u000A' // U+000A LINE FEED (LF)

//https://developer.mozilla.org/en-US/docs/Web/API/EventSource
// https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events


/**
 * SSE event source
 *
 * url: the URL providing the event stream.
 * readyState: the state of this EventSource object's connection.
 * withCredentials:
 */
interface SseEventSource {
    val url: String
    val withCredentials: Boolean

    suspend fun open()

    /**
     * Aborts any instances of the fetch algorithm started for this EventSource object,
     * and sets the readyState attribute to CLOSED.
     */
    suspend fun close()

    val events: Flow<SseEvent>

    val state: Flow<State>

    data class State(
        val ready: ReadyState = CONNECTING,
        val isFailed: Boolean = false,
        val statusCode: Int = -1,
        val throwable: Throwable? = null
    ) {
        val isError: Boolean by lazy {
            throwable != null
        }
    }
}

enum class ReadyState(val value: UShort) {
    CONNECTING(0u),
    OPEN(1u),
    CLOSED(2u),
}

class SseEventSourceImpl( // needs to be different due to name clash in JS
    override val url: String,
    override val withCredentials: Boolean = false, //
    internal var reconnectionTime: Duration = 10.seconds,
    internal val provider: SseLineStream.Provider,
    context: CoroutineContext = Job(),
    internal val isStreamFailed: (SseLineStream.State) -> Boolean = { false }
) : SseEventSource {
    override suspend fun open() {
        mutex.withLock {
            tryConnect()
        }
    }

    internal val mutex = Mutex()

    override suspend fun close() {
        mutex.withLock {
            readyState = CLOSED
            resetBuffer()
            supervisor.cancelChildren()
        }
    }

    internal var readyState: ReadyState
        get() = _state.value.ready
        set(value) {
            _state.value = _state.value.copy(ready = value)
        }

    override val events: Flow<SseEvent>
        get() = _messages


    internal val _state = MutableStateFlow(SseEventSource.State())
    override val state: Flow<SseEventSource.State>
        get() = _state

    val isFailed: Boolean
        get() = false

    internal var lastEventId: String? = null//  This must initially be the empty string.
    internal var buffer = SseBuffer()

    internal val _messages =
        MutableSharedFlow<SseEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    private val supervisor: Job = SupervisorJob()
    internal val scope: CoroutineScope = CoroutineScope(context = context + supervisor)
    internal var collectJob: Job? = null

    internal fun resetBuffer() {
        buffer = SseBuffer()
    }
}

