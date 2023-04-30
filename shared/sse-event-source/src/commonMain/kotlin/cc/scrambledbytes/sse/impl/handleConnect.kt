package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.util.debugTrace
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal suspend fun SseEventSourceImpl.handleConnect() {
    debugTrace("tryConnect")
    val stream = createStream()
        ?: return

    lineScope.launch {

        launch {
            connectStream(stream)
        }

        waitForConnection(stream)
    }.invokeOnCompletion {
        debugTrace("Line stream completed($it), cleanup in $stream")
        stream.close()
    }
}

private suspend fun SseEventSourceImpl.waitForConnection(
    stream: SseLineStream,
) {
    stream.state // this will change at most once
        .filterNotNull()
        .collect {
            debugTrace("Connected to SseLineStream: $it")
            schedule(SseEventSourceImpl.Intent.Connected(it, stream.lines))
        }
}

private suspend fun SseEventSourceImpl.connectStream(
    source: SseLineStream,
) {
    try {
        source.connect()
    } catch (e: Exception) {
        debugTrace("Failed connection: $e")
        schedule(SseEventSourceImpl.Intent.HandleError(source, e))
    }
}

private suspend fun SseEventSourceImpl.createStream(): SseLineStream? =
    try {
        provider.create(url, lastEventId)
    } catch (e: Exception) {
        _state.value = _state.value.copy(
            ready = CLOSED,
            isFailed = true,
            throwable = e,
        )
        null
    }


