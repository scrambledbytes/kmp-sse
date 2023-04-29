package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventStream
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal fun SseEventSourceImpl.tryConnect() {
    if (readyState == ReadyState.CLOSED)
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
            try {
                newSource.connect()
            } catch (e: Exception) {
                println("Failed connection: $e")
            }
        }

        println("Connecting connect")

        newSource.state
            .filterNotNull()
            .collectLatest {
                when {
                    newSource.isFailed -> handleFail(newSource)
                    newSource.isRetry -> handleRetryConnection(newSource)
                    else -> {
                        readyState = ReadyState.OPEN
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