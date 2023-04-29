package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventStream

fun SseEventSourceImpl.handleError(
    newSource: SseEventStream,
    error: Exception
) {
    // TODO cancellation exception?
    _state.value = _state.value.copy(
        ready = ReadyState.CLOSED,
        statusCode = newSource.statusCode,
        throwable = error,
    )

    close()
}
