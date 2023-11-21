package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventSourceImpl.Intent.ConnectDelayed
import cc.scrambledbytes.sse.SseLineStream
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun SseEventSourceImpl.handleError(
    newSource: SseLineStream,
    error: Throwable
) {
    if (error is CancellationException) {
        return
    }


    val streamState = newSource.state.value

    val isStreamFailed = if (streamState != null) {
        streamState.isFailed || isStreamFailed(streamState)
    } else {
        false
    }

    internalState.value = internalState.value.copy(
        ready = CLOSED,
        isFailed = isStreamFailed || isFatalError(error),
        statusCode = newSource.statusCode(),
        throwable = error,
    )
    stop()

    // no tasks will be execute when failed
    schedule(ConnectDelayed)
}
