package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState
import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import java.util.concurrent.CancellationException

internal suspend fun SseEventSourceImpl.handleError(
    newSource: SseLineStream,
    error: Exception
) {
    if (error is CancellationException)
        return

    _state.value = _state.value.copy(
        ready = CLOSED,
        statusCode = newSource.statusCode(),
        throwable = error,
    )

//    handleRetryConnection(streamState = newSource.state)
}
// TODO reconnect after error (i.e., connection error)
// TODO fix race condition