package cc.scrambledbytes.sse

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
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

private const val LF = '\u000A' // U+000A LINE FEED (LF)

//https://developer.mozilla.org/en-US/docs/Web/API/EventSource
// https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events
class SseClient(
    val url: String,
    val withCredentials: Boolean, // TODO
    var reconnectionTime: Duration = 10.seconds,
    val provider: SseEventStream.Provider,
    val contextProvider: () -> CoroutineContext
) {

    var lastEventId: String = "" //  This must initially be the empty string.

    // TODO extract buffer
    var bufferEventType: String = "" //
    var bufferLastEventId: String = "" // id
    var bufferData: String = "" // data buffer

    private val _messages =
        MutableSharedFlow<SseEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

    private val supervisor: Job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(context = contextProvider() + supervisor)
    private var collectJob: Job? = null

    fun connect() {
        scope.launch {
            tryConnect()
        }
    }

    fun disconnect() {
        supervisor.cancelChildren()
    }

    private suspend fun tryConnect() {
        if (_readyState.value == ReadyState.CLOSED)
            return


        collectJob?.cancel()
        var source: SseEventStream? = null

        collectJob = scope.launch {
            println("Collect job started")
            // TODO mutex
            // TODO refactoring
            val newSource = provider.create(url, lastEventId)
            println("Source created")
            source = newSource

            launch {
                newSource.connect()
            }

            println("Connecting connect")

            newSource.state
                .filterNotNull()
                .collectLatest {
                    println("Got state: $it ${newSource.isFailed} ${newSource.isRetry} ")
                    when {
                        newSource.isFailed -> handleFail(newSource)
                        newSource.isRetry -> handleRetryConnection(newSource)
                        else -> {
                            println("Connecting listener")
                            _readyState.value = ReadyState.OPEN
                            newSource.events
                                .collect { line ->
                                    processLine(line)
                                }
                        }
                    }
                }
        }

        collectJob?.invokeOnCompletion {
            println("Collect job finished")
            source?.close()
        }

        collectJob?.join()

        println("Finished")
    }

    /**
     * When a user agent is to fail the connection, the user agent must queue a task which, if the readyState attribute
     * is set to a value other than CLOSED, sets the readyState attribute to CLOSED and fires an event named error at
     * the EventSource object. Once the user agent has failed the connection, it does not attempt to reconnect.
     */
    private fun handleFail(newSource: SseEventStream) {
        println("Handle fail")
        newSource.fireError()
        close()
    }

    private suspend fun handleRetryConnection(
        eventStream: SseEventStream
    ) {
        println("handleRetryConnection")
        if (_readyState.value == ReadyState.CLOSED)
            return

        eventStream.fireError()

        _readyState.value = ReadyState.CONNECTING

        delay(reconnectionTime)

        /*
        TODO Optionally, wait some more. In particular, if the previous attempt failed, then user agents might introduce an exponential backoff delay to avoid overloading a potentially already overloaded server. Alternatively, if the operating system has reported that there is no network connectivity, user agents might wait for the operating system to announce that the network connection has returned before retrying.
       */

        tryConnect()
    }

    val events: Flow<SseEvent>
        get() = _messages

    private val _readyState = MutableStateFlow(ReadyState.CONNECTING)
    val readyState: Flow<ReadyState>
        get() = _readyState

    /**
     * https://html.spec.whatwg.org/multipage/server-sent-events.html#event-stream-interpretation
     */
    private suspend fun processLine(line: String) {
        println("Processing: $line")
        when {
            line.isBlank() -> dispatchEvent()
            line.startsWith(":") -> Unit // Ignore the line.
            ":" in line -> {
                val (name, value) = line.split(":")
                processField(
                    name = name,
                    value.removePrefix(" "), //If value starts with a U+0020 SPACE character, remove it from value.
                )
            }

            else -> processField(name = line, fieldValue = "")
        }
    }

    private fun processField(name: String, fieldValue: String) {
        println("processField: name=$name, value=$fieldValue")
        when (name) {
            "event" -> handleEvent(fieldValue)
            "data" -> handleData(fieldValue)
            "id" -> handleId(fieldValue)
            "retry" -> handleRetry(fieldValue)
            else -> Unit // The field is ignored.
        }
    }

    /**
     * Set the event type buffer to field value.
     */
    private fun handleEvent(fieldValue: String) {
        println("handleEvent: $fieldValue")
        bufferEventType = fieldValue
    }

    /**
     * Append the field value to the data buffer, then append a single U+000A LINE FEED (LF) character to the data buffer.
     */
    private fun handleData(fieldValue: String) {
        bufferData = bufferData + fieldValue + LF
    }

    /*
     * If the field value does not contain U+0000 NULL, then set the last event ID buffer to the field value.
     *
     * Otherwise, ignore the field.
     */
    private fun handleId(fieldValue: String) {
        if (Character.MIN_VALUE in fieldValue)
            return

        bufferLastEventId = fieldValue
    }

    /**
     * If the field value consists of only ASCII digits, then interpret the field value as an integer in base ten, and
     * set the event stream's reconnection time to that integer.
     *
     * Otherwise, ignore the field.
     */
    private fun handleRetry(fieldValue: String) {
        val newReconnectionTime = fieldValue.toIntOrNull(10)
            ?: return

        reconnectionTime = newReconnectionTime.milliseconds
    }

    private suspend fun dispatchEvent() {
        println("Dispatch $bufferEventType $bufferData")
        // 1 Set the last event ID string of the event source to the value of the last event ID buffer. The buffer does not get reset, so the last event ID string of the event source remains set to this value until the next time it is set by the server.
        lastEventId = bufferLastEventId

        // 2 If the data buffer is an empty string, set the data buffer and the event type buffer to the empty string and return.
        if (bufferData.isBlank()) {
            println("No buffer")
            bufferData = ""
            bufferEventType = ""
            return
        }

        // 3 If the data buffer's last character is a U+000A LINE FEED (LF) character, then remove the last character from the data buffer.
        bufferData = bufferData.removeSuffix(LF.toString())

        // 4 Let event be the result of creating an event using MessageEvent, in the relevant realm of the EventSource object.

        // 5 Initialize event's type attribute to message, its data attribute to data, its origin attribute to the serialization of the origin of the event stream's final URL (i.e., the URL after redirects), and its lastEventId attribute to the last event ID string of the event source.

        // 6 If the event type buffer has a value other than the empty string, change the type of the newly created event to equal the value of the event type buffer.
        val message = SseEvent(
            data = bufferData,
            type = bufferEventType,
            lastEventId = lastEventId,
            origin = url, // TODO
        )

        // 7 Set the data buffer and the event type buffer to the empty string.
        bufferData = ""
        bufferEventType = ""

        // 8 Queue a task which, if the readyState attribute is set to a value other than CLOSED, dispatches the newly created event at the EventSource object.
        if (_readyState.value != ReadyState.CLOSED) {
            println("Emitting message: $message")
            _messages.emit(message)
        } else {
            println("Omit message: $message")
        }
    }

    fun close() {
        collectJob?.cancel()
        _readyState.value = ReadyState.CLOSED
    }

    enum class ReadyState(val value: UShort) {
        CONNECTING(0u),
        OPEN(1u),
        CLOSED(2u),
    }
}

