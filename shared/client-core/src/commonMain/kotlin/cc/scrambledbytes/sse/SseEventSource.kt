package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.client.tryConnect
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

//    1 Let ev be a new EventSource object.
//
//    2 Let settings be ev's relevant settings object.
//
//    3 Let urlRecord be the result of parsing url with settings's API base URL and settings's API URL character encoding.
//
//    4 If urlRecord is failure, then throw a "SyntaxError" DOMException.
//
//    5 Set ev's url to urlRecord.
//
//    6 Let corsAttributeState be Anonymous.
//
//    7 If the value of eventSourceInitDict's withCredentials member is true, then set corsAttributeState to Use Credentials and set ev's withCredentials attribute to true.
//
//    8 Let request be the result of creating a potential-CORS request given urlRecord, the empty string, and corsAttributeState.
//
//    9 Set request's client to settings.
//
//    10 User agents may set (`Accept`, `text/event-stream`) in request's header list.
//
//    11 Set request's cache mode to "no-store".
//
//    12 Set request's initiator type to "other".
//
//    13 Set ev's request to request.
//
//    14 Let processEventSourceEndOfBody given response res be the following step: if res is not a network error, then reestablish the connection.
//
//    15 Fetch request, with processResponseEndOfBody set to processEventSourceEndOfBody and processResponse set to the following steps given response res:
//
//    16 If res is an aborted network error, then fail the connection.
//
//    17 Otherwise, if res is a network error, then reestablish the connection, unless the user agent knows that to be futile, in which case the user agent may fail the connection.
//
//    18 Otherwise, if res's status is not 200, or if res's `Content-Type` is not `text/event-stream`, then fail the connection.
//
//    19 Otherwise, announce the connection and interpret res's body line by line.
//
//    20 Return ev.

internal const val LF = '\u000A' // U+000A LINE FEED (LF)

//https://developer.mozilla.org/en-US/docs/Web/API/EventSource
// https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events
class SseEventSource(
    internal val url: String,
    private val withCredentials: Boolean = false, // TODO
    internal var reconnectionTime: Duration = 10.seconds,
    internal val provider: SseEventStream.Provider,
    private val context: CoroutineContext = Job()
) {
    fun connect() {
        tryConnect()
    }

    fun disconnect() {
        close()
    }

    val events: Flow<SseEvent>
        get() = _messages


    internal val _readyState = MutableStateFlow(ReadyState.CONNECTING)
    val state: Flow<ReadyState>
        get() = _readyState


    internal var lastEventId: String = "" //  This must initially be the empty string.
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

    internal fun close() {
        resetBuffer()
        supervisor.cancelChildren()
        _readyState.value = ReadyState.CLOSED
    }

    enum class ReadyState(val value: UShort) {
        CONNECTING(0u),
        OPEN(1u),
        CLOSED(2u),
    }
}

