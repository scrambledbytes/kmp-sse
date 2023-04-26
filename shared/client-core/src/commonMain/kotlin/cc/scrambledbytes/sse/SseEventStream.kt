package cc.scrambledbytes.sse

import kotlinx.coroutines.flow.Flow

// closed event streams should be re-connected
// failed event stream should not be re-connected
class SseEventStream(
    private val status: Int, // 200 -> OK, 301/307 -> Redirect,
    private val contentType: String, // needs to be 'text/event-stream' -> fail connection
    private val isAborted: Boolean, // aborted error or reconnection is futile
    private val isError: Boolean,
    private val onClose: () -> Unit, // closes the event stream
    private val onError: (String) -> Unit, // callback when an error happens
    val body: Flow<String>, // UTF8 encoded flow
) {
    val isRetry: Boolean by lazy {
        isError && !isFailed
    }

    val isFailed: Boolean by lazy { // -> fail the connection
        when {
            contentType != "text/event-stream" -> false
            isAborted -> false
            status != 200 -> false
            else -> true
        }
    }

    fun close() {
        onClose()
    }

    fun fireError() {
        onError("error=$isError, aborted=$isAborted,  status=$status, type=$contentType")
    }

    interface Provider {

        fun setOtherInitiator()

        // Set request's cache mode to "no-store".  "Cache-Control", "no-cache"
        fun setNoCacheControl()

        // Accept: text/event-stream
        fun setAcceptTextStream()

        fun setUrl(url: String): Provider // TODO url parsing

        // Last-Event-Id:
        fun setLastEventId(lastEventId:String)

        /**
         * Let lastEventId be the EventSource object's last event ID string, encoded as UTF-8. Set (`Last-Event-ID`, lastEventIDValue) in request's header list.
         */
        suspend fun open(): SseEventStream
    }
}