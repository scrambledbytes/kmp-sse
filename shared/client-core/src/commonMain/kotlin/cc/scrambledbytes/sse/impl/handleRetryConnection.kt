package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventStream
import kotlinx.coroutines.delay

internal suspend fun SseEventSourceImpl.handleRetryConnection(
    eventStream: SseEventStream
) {
    if (readyState == CLOSED)
        return

    // TODO fire error:


    readyState = CONNECTING

    delay(reconnectionTime)

    /*
    TODO Optionally, wait some more. In particular, if the previous attempt failed, then user agents might introduce an exponential backoff delay to avoid overloading a potentially already overloaded server. Alternatively, if the operating system has reported that there is no network connectivity, user agents might wait for the operating system to announce that the network connection has returned before retrying.
   */

    tryConnect()
}
