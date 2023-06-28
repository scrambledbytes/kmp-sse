package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.impl.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
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
    val url: SseUrl
    val withCredentials: Boolean

    suspend fun open()

    /**
     * Aborts any instances of the fetch algorithm started for this EventSource object,
     * and sets the readyState attribute to CLOSED.
     *
     * Can be re-opened if not failed
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

fun SseEventSource(
    url: String,
    streamProvider: SseLineStream.Provider,
    context: CoroutineContext = Job(),
    withCredentials: Boolean = false,
    delayProvider: (Int) -> Duration? = { 10.seconds },
    isStreamFailed: (SseLineStream.ConnectionState) -> Boolean = { false },
    isFatalError: (Throwable) -> Boolean = { false },
    isConnectedProvider: (suspend () -> Flow<Boolean>)? = null,
): SseEventSource =
    SseEventSourceImpl(
        urlString = url,
        provider = streamProvider,
        context = context,
        withCredentials = withCredentials,
        delayProvider = delayProvider,
        isStreamFailed = isStreamFailed,
        isFatalError = isFatalError,
        isConnectedProvider = isConnectedProvider,
    )


internal class SseEventSourceImpl(
    // needs to be different due to name clash in JS
    urlString: String,
    internal val provider: SseLineStream.Provider,
    context: CoroutineContext,
    override val withCredentials: Boolean,
    internal var delayProvider: (Int) -> Duration?,
    internal val isStreamFailed: (SseLineStream.ConnectionState) -> Boolean,
    internal val isFatalError: (Throwable) -> Boolean,
    internal val isConnectedProvider: (suspend () -> Flow<Boolean>)?,
) : SseEventSource {
    internal var connectionAttempt: Int = 0
    internal var reconnectionTime: Duration? = null

    override val url: SseUrl = provider.parse(urlString)
    override suspend fun open() {
        require(!_state.value.isFailed) {"Attempted to open a failed EventSource"}
        schedule(Intent.Connect)
    }

    private val intentScope: CoroutineScope = CoroutineScope(context = context)
    private val channel = Channel<Intent>(capacity = Channel.UNLIMITED)
    internal val intentJob: Job = intentScope.launch {
        channel.consumeAsFlow()
            .collect { handleIntent(it) }
            // TODO error handling
    }

    internal suspend fun schedule(intent: Intent) {
        channel.send(intent)
    }

    private val supervisor: Job = SupervisorJob()
    internal val lineScope = CoroutineScope(context + supervisor)

    private suspend fun handleIntent(
        intent: Intent
    ) {
        if (_state.value.isFailed)
            return

        when (intent) {
            Intent.Connect -> handleConnect()
            is Intent.Connected -> handleConnected(intent)
            Intent.Dispatch -> handleDispatch()
            is Intent.HandleError -> handleError(intent.source, intent.throwable)
            is Intent.ProcessLine -> handleProcessLine(intent.line)
            Intent.ConnectDelayed -> handleDelayedConnectionAttempt()
        }
    }

    override suspend fun close() {
        readyState = CLOSED
        stop()
    }

    internal fun stop() {
        resetBuffer()
        supervisor.cancelChildren()
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

    internal var lastEventId: String? = null//  This must initially be the empty string.
    internal var buffer = SseBuffer()

    internal val _messages =
        MutableSharedFlow<SseEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    internal fun resetBuffer() {
        buffer = SseBuffer()
    }

    internal sealed interface Intent {
        object Connect : Intent
        object ConnectDelayed : Intent
        object Dispatch : Intent
        data class ProcessLine(val line: SseLine) : Intent

        data class HandleError(
            val source: SseLineStream,
            val throwable: Throwable
        ) : Intent

        data class Connected(
            val state: SseLineStream.ConnectionState,
            val lines: Flow<SseLine>,
        ) : Intent
    }
}
