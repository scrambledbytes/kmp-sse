package cc.scrambledbytes.sse.client

import cc.scrambledbytes.sse.SseClient
import cc.scrambledbytes.sse.SseEventStream
import kotlinx.coroutines.delay

internal suspend fun SseClient.handleRetryConnection(
    eventStream: SseEventStream
) {
    if (_readyState.value == SseClient.ReadyState.CLOSED)
        return

    eventStream.fireError()

    _readyState.value = SseClient.ReadyState.CONNECTING

    delay(reconnectionTime)

    /*
    TODO Optionally, wait some more. In particular, if the previous attempt failed, then user agents might introduce an exponential backoff delay to avoid overloading a potentially already overloaded server. Alternatively, if the operating system has reported that there is no network connectivity, user agents might wait for the operating system to announce that the network connection has returned before retrying.
   */

    tryConnect()
}
