package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventSourceImpl.Intent.ConnectDelayed
import cc.scrambledbytes.sse.SseLineStream
import java.util.concurrent.CancellationException

internal suspend fun SseEventSourceImpl.handleError(
    newSource: SseLineStream,
    error: Throwable
) {
    if (error is CancellationException)
        return

    // TODO custom fail condition

    _state.value = _state.value.copy(
        ready = CLOSED,
        isFailed = newSource.state.value?.isFailed == true,
        statusCode = newSource.statusCode(),
        throwable = error,
    )
    stop()

    schedule(ConnectDelayed)
}