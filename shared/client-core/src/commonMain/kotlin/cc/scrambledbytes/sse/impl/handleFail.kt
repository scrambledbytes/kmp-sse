package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.SseEventSource
import cc.scrambledbytes.sse.SseEventStream

/**
 * When a user agent is to fail the connection, the user agent must queue a task which, if the readyState attribute
 * is set to a value other than CLOSED, sets the readyState attribute to CLOSED and fires an event named error at
 * the EventSource object. Once the user agent has failed the connection, it does not attempt to reconnect.
 */
internal fun SseEventSource.handleFail(
    newSource: SseEventStream
) {
    // TODO fire error newSource.fireError()
    close()
}
