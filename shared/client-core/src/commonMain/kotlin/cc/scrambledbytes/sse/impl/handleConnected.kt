package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.OPEN
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLine
import cc.scrambledbytes.sse.SseLineStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal suspend fun SseEventSourceImpl.handleConnected(
    intent: SseEventSourceImpl.Intent.Connected
) {
    val state = intent.state
    when {
        state.isFailed || isStreamFailed(state) -> handleFail(state)
        state.isRetry -> handleRetryConnection(state)
        else -> handleOpen(state, intent.lines)
    }
}

/**
 * When a user agent is to fail the connection, the user agent must queue a task which, if the readyState attribute
 * is set to a value other than CLOSED, sets the readyState attribute to CLOSED and fires an event named error at
 * the EventSource object. Once the user agent has failed the connection, it does not attempt to reconnect.
 */
private fun SseEventSourceImpl.handleFail(
    state: SseLineStream.ConnectionState
) {
    _state.value = _state.value.copy(
        ready = CLOSED,
        isFailed = true,
        statusCode = state.statusCode
    )
    stop()
}


private suspend fun SseEventSourceImpl.handleRetryConnection(
    streamState: SseLineStream.ConnectionState
) {
    _state.value = _state.value.copy(
        ready = CLOSED,
        isFailed = streamState.isFailed,
        statusCode = streamState.statusCode,
    )
    stop()

    schedule(SseEventSourceImpl.Intent.ConnectDelayed)
}

private suspend fun SseEventSourceImpl.handleOpen(
    connectionState: SseLineStream.ConnectionState,
    lines: Flow<SseLine>
) {
    _state.value = _state.value.copy(
        ready = OPEN,
        statusCode = connectionState.statusCode,
    )

    connectionAttempt = 0

    lineScope.launch {
        lines.collect { line ->
            schedule(SseEventSourceImpl.Intent.ProcessLine(line))
        }
    }
}
