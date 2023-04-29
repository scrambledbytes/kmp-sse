package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.OPEN
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream

internal suspend fun SseEventSourceImpl.handleOpen(
    it: SseLineStream.State,
    newSource: SseLineStream
) {
    _state.value = _state.value.copy(
        ready = OPEN,
        statusCode = it.statusCode,
    )
    newSource.lines
        .collect { line ->
            processLine(line.value)
        }
}