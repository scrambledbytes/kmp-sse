package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl

internal suspend fun SseEventSourceImpl.handleDispatch() {
    // 1 Set the last event ID string of the event source to the value of the last event ID buffer.
    // The buffer does not get reset, so the last event ID string of the event source remains set to
    // this value until the next time it is set by the server.
    lastEventId = buffer.lastEventId

    // 2 If the data buffer is an empty string, set the data buffer and the event type buffer to the
    // empty string and return.
    if (buffer.isEmpty && ignoreEmptyData) {
        resetBuffer()
        return
    }

    // 3 If the data buffer's last character is a U+000A LINE FEED (LF) character, then remove the
    // last character from the data buffer.


    // 4 Let event be the result of creating an event using MessageEvent, in the relevant realm of
    // the EventSource object.

    // 5 Initialize event's type attribute to message, its data attribute to data, its origin
    // attribute to the serialization of the origin of the event stream's final URL (i.e., the URL
    // after redirects), and its lastEventId attribute to the last event ID string of the event
    // source.

    // 6 If the event type buffer has a value other than the empty string, change the type of the
    // newly created event to equal the value of the event type buffer.
    val message = buffer.toSseEvent()

    // 7 Set the data buffer and the event type buffer to the empty string.
    resetBuffer()

    // 8 Queue a task which, if the readyState attribute is set to a value other than CLOSED,
    // dispatches the newly created event at the EventSource object.
    if (readyState != CLOSED) {
        internalMessages.emit(message)
    }
}
