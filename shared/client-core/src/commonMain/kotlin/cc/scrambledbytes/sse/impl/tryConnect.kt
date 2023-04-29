package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState.OPEN
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventStream
import cc.scrambledbytes.sse.util.debugTrace
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal fun SseEventSourceImpl.tryConnect() {
    if (isFailed)
        return

    collectJob?.cancel()
    var source: SseEventStream? = null

    collectJob = scope.launch {
        debugTrace("Collect job started")
        // TODO mutex
        // TODO refactoring
        val newSource = provider.create(url, lastEventId)
        debugTrace("Source created")
        source = newSource

        launch {
            try {
                newSource.connect()
            } catch (e: Exception) {
                debugTrace("Failed connection: $e")
                handleConnectionError(newSource, e)
            }
        }

        debugTrace("Connecting")

        newSource.state
            .filterNotNull()
            .collectLatest {
                debugTrace("New stream state: $it")
                when {
                    it.isFailed -> handleFail(it)
                    it.isRetry -> handleRetryConnection(it)
                    else -> handleOpen(it, newSource)
                }
            }
    }

    collectJob?.invokeOnCompletion {
        debugTrace("Collect job finished, cleanup")
        source?.close()
    }

    debugTrace("Finished")
}

fun SseEventSourceImpl.handleConnectionError(
    newSource: SseEventStream,
    error: Exception
) {
    // TODO cancellation exception?
    val streamState = newSource.state.value
    _state.value = _state.value.copy(

    )


}

