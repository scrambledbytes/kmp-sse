package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.util.debugTrace

internal suspend fun SseEventSourceImpl.handleRetryConnection(
    streamState: SseLineStream.ConnectionState
) {
    _state.value = _state.value.copy(
        ready = CLOSED,
        isFailed = streamState.isFailed,
        statusCode = streamState.statusCode,
    )

    debugTrace("Reconnecting...")
    if (isFailed)
        return

    _state.value = _state.value.copy(
        ready = CONNECTING,
        statusCode = -1,
    )

    /*
    TODO Optionally, wait some more. In particular, if the previous attempt failed, then user agents might introduce an exponential backoff delay to avoid overloading a potentially already overloaded server. Alternatively, if the operating system has reported that there is no network connectivity, user agents might wait for the operating system to announce that the network connection has returned before retrying.
   */

    tryConnect()
}
