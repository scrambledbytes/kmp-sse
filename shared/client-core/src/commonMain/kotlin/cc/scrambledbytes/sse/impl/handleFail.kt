package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream

/**
 * When a user agent is to fail the connection, the user agent must queue a task which, if the readyState attribute
 * is set to a value other than CLOSED, sets the readyState attribute to CLOSED and fires an event named error at
 * the EventSource object. Once the user agent has failed the connection, it does not attempt to reconnect.
 */
internal suspend fun SseEventSourceImpl.handleFail(
    state: SseLineStream.State
) {
    _state.value = _state.value.copy(
        ready = CLOSED,
        isFailed = true,
        statusCode = state.statusCode
    )
    close()
}
