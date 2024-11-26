package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.impl.handleConnect
import cc.scrambledbytes.sse.impl.handleConnected
import cc.scrambledbytes.sse.impl.handleDelayedConnectionAttempt
import cc.scrambledbytes.sse.impl.handleDispatch
import cc.scrambledbytes.sse.impl.handleError
import cc.scrambledbytes.sse.impl.handleProcessLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
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
    /**
     * The url of the SseStream.
     */
    val url: SseUrl

    /**
     * If credentials are sent.
     */
    val withCredentials: Boolean

    /**
     * Extra headers to be added when connecting to the stream.
     */
    val extraHeaders: Map<String, String>

    /**
     * Flow of incoming SseEvents.
     */
    val events: Flow<SseEvent>

    /**
     * The current state of the SseEventSource.
     */
    val state: StateFlow<State>

    /**
     * Connect the SseEventSource. Initiates a request.
     */
    suspend fun open()

    /**
     * Aborts any instances of the fetch algorithm started for this EventSource object,
     * and sets the readyState attribute to CLOSED.
     *
     * Can be re-opened if not failed
     */
    suspend fun close()


    /**
     * The state of the SseEventSource.
     */
    data class State(
        /**
         * In which ready state the SseEventSource currently is.
         */
        val ready: ReadyState = CONNECTING,
        /**
         * Whether the SseEventSource is failed or not.
         */
        val isFailed: Boolean = false,
        /**
         * The HTTP Status Code of the last request. -1 when
         */
        val statusCode: Int = -1,
        /**
         * The error that happened on the request, if any.
         */
        val throwable: Throwable? = null
    ) {
        /**
         * Whether the SseEventSource is in an error state.
         */
        val isError: Boolean by lazy {
            throwable != null
        }
    }
}

/**
 * The ready state of the SseEventSource.
 */
enum class ReadyState(
    @Suppress("UndocumentedPublicProperty")
    val value: UShort
) {
    CONNECTING(0u),
    OPEN(1u),
    CLOSED(2u),
}

/**
 * Factory method for SseEventSources.
 */
fun SseEventSource(
    url: String,
    streamProvider: SseLineStream.Provider,
    extraHeaders: Map<String, String> = emptyMap(),
    context: CoroutineContext = Job(),
    withCredentials: Boolean = false,
    ignoreEmptyData: Boolean = false, //
    delayProvider: (Int) -> Duration? = { 10.seconds },
    isStreamFailed: (SseLineStream.ConnectionState) -> Boolean = { false },
    isFatalError: (Throwable) -> Boolean = { false },
    isConnectedProvider: (suspend () -> Flow<Boolean>)? = null,
): SseEventSource =
    SseEventSourceImpl(
        urlString = url,
        provider = streamProvider,
        extraHeaders = extraHeaders,
        context = context,
        withCredentials = withCredentials,
        ignoreEmptyData = ignoreEmptyData, // allow events with empty data
        delayProvider = delayProvider,
        isStreamFailed = isStreamFailed,
        isFatalError = isFatalError,
        isConnectedProvider = isConnectedProvider,
    )


internal class SseEventSourceImpl(
    // needs to be different due to name clash in JS
    urlString: String,
    internal val provider: SseLineStream.Provider,
    override val extraHeaders: Map<String, String>,
    context: CoroutineContext,
    override val withCredentials: Boolean,
    internal val ignoreEmptyData: Boolean,
    internal var delayProvider: (Int) -> Duration? = { 10.seconds },
    internal val isStreamFailed: (SseLineStream.ConnectionState) -> Boolean =  { false },
    internal val isFatalError: (Throwable) -> Boolean =  { false },
    internal val isConnectedProvider: (suspend () -> Flow<Boolean>)?=null,
) : SseEventSource {
    internal var connectionAttempt: Int = 0
    internal var reconnectionTime: Duration? = null

    internal var readyState: ReadyState
        get() = internalState.value.ready
        set(value) {
            internalState.value = internalState.value.copy(ready = value)
        }

    override val events: Flow<SseEvent>
        get() = internalMessages


    internal val internalState = MutableStateFlow(SseEventSource.State())
    override val state: StateFlow<SseEventSource.State>
        get() = internalState

    internal var lastEventId: String? = null//  This must initially be the empty string.
    internal var buffer = SseBuffer()

    internal val internalMessages =
        MutableSharedFlow<SseEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    private val intentScope: CoroutineScope = CoroutineScope(context = context)
    private val channel = Channel<Intent>(capacity = Channel.UNLIMITED)
    internal val intentJob: Job = intentScope.launch {
        channel.consumeAsFlow()
            .collect { handleIntent(it) }
        // TODO error handling
    }

    private val supervisor: Job = SupervisorJob()
    internal val lineScope = CoroutineScope(context + supervisor)

    override val url: SseUrl = provider.parse(urlString)
    override suspend fun open() {
        require(!internalState.value.isFailed) { "Attempted to open a failed EventSource" }
        schedule(Intent.Connect)
    }



    internal suspend fun schedule(intent: Intent) {
        channel.send(intent)
    }



    private suspend fun handleIntent(
        intent: Intent
    ) {
        if (internalState.value.isFailed) {
            return
        }

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



    internal fun resetBuffer() {
        buffer = SseBuffer()
    }

    internal sealed interface Intent {
        data object Connect : Intent
        data object ConnectDelayed : Intent
        data object Dispatch : Intent
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
