package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal suspend fun SseEventSourceImpl.handleConnect() {
    val stream = createStream() ?: return

    lineScope.launch {
        launch {
            connectStream(stream)
        }

        waitForConnection(stream)
    }.invokeOnCompletion {
        stream.close()
    }
}

private suspend fun SseEventSourceImpl.waitForConnection(
    stream: SseLineStream,
) {
    stream.state // this will change at most once
        .filterNotNull()
        .collect {
            schedule(SseEventSourceImpl.Intent.Connected(it, stream.lines))
        }
}

private suspend fun SseEventSourceImpl.connectStream(
    source: SseLineStream,
) {
    try {
        source.connect()
    } catch (e: Exception) {
        schedule(SseEventSourceImpl.Intent.HandleError(source, e))
    }
}

private suspend fun SseEventSourceImpl.createStream(): SseLineStream? =
    try {
        provider.create(url, lastEventId, withCredentials, extraHeaders)
    } catch (e: Exception) {
        internalState.value = internalState.value.copy(
            ready = CLOSED,
            isFailed = true,
            throwable = e,
        )
        null
    }
