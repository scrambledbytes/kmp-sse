package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.util.debugTrace
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal suspend fun SseEventSourceImpl.tryConnect() {
    debugTrace("tryConnect")
    if (isFailed)
        return

    collectJob?.cancel()
    var source: SseLineStream? = null

    collectJob = scope.launch {
        debugTrace("Collect job started")
        // TODO mutex
        // TODO refactoring
        val newSource = provider.create(url, lastEventId)
        debugTrace("Source created")
        source = newSource

        launch {
            try {
                if (isActive) {
                    newSource.connect() // blocking call
                }
            } catch (e: Exception) {
                debugTrace("Failed connection: $e")
                handleError(newSource, e)
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
        debugTrace("Collect job($collectJob) finished, cleanup")
        source?.close()
    }

    debugTrace("Finished")
}


