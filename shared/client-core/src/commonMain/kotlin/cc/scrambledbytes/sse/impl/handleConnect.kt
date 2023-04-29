package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.util.debugTrace
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal suspend fun SseEventSourceImpl.handleConnect() {
    debugTrace("tryConnect")
    val source: SseLineStream = provider.create(url, lastEventId)

    val job = lineScope.launch {
        debugTrace("Collect job started")
        launch {
            try {
                if (isActive) {
                    source.connect()
                }
            } catch (e: Exception) {
                debugTrace("Failed connection: $e")
                schedule(SseEventSourceImpl.Intent.HandleError(source, e))
            }
        }

        debugTrace("Connecting")

        source.state // this will change at most once
            .filterNotNull()
            .collect {
                debugTrace("Connected to SseLineStream: $it")
                schedule(SseEventSourceImpl.Intent.Connected(it, source.lines))
            }
    }

    job.invokeOnCompletion {
        debugTrace("Collect $job finished, cleanup in $source")
        source.close()
    }
}


