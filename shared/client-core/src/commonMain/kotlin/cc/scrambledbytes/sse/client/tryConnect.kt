package cc.scrambledbytes.sse.client

import cc.scrambledbytes.sse.SseClient
import cc.scrambledbytes.sse.SseEventStream
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal fun SseClient.tryConnect() {
    if (_readyState.value == SseClient.ReadyState.CLOSED)
        return

    collectJob?.cancel()
    var source: SseEventStream? = null

    collectJob = scope.launch {
        println("Collect job started")
        // TODO mutex
        // TODO refactoring
        val newSource = provider.create(url, lastEventId)
        println("Source created")
        source = newSource

        launch {
            newSource.connect()
        }

        println("Connecting connect")

        newSource.state
            .filterNotNull()
            .collectLatest {
                when {
                    newSource.isFailed -> handleFail(newSource)
                    newSource.isRetry -> handleRetryConnection(newSource)
                    else -> {
                        _readyState.value = SseClient.ReadyState.OPEN
                        newSource.events
                            .collect { line ->
                                processLine(line)
                            }
                    }
                }
            }
    }

    collectJob?.invokeOnCompletion {
        println("Collect job finished")
        source?.close()
    }

    println("Finished")
}