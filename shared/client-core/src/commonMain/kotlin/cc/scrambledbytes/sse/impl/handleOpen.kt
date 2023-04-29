package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState
import cc.scrambledbytes.sse.ReadyState.OPEN
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventStream

internal suspend fun SseEventSourceImpl.handleOpen(
    it: SseEventStream.State,
    newSource: SseEventStream
) {
    _state.value = _state.value.copy(
        ready = OPEN,
        statusCode = it.statusCode,
    )
    newSource.events
        .collect { line ->
            processLine(line)
        }
}