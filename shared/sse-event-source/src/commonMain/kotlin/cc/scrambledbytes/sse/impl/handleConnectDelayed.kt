package cc.scrambledbytes.sse.impl


import cc.scrambledbytes.sse.ReadyState.CLOSED
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventSourceImpl.Intent.Connect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal fun SseEventSourceImpl.handleDelayedConnectionAttempt() {
    internalState.value = internalState.value.copy(
        ready = CONNECTING
    )

    lineScope.launch {
        connectionAttempt++

        val providedDelay = delayProvider(connectionAttempt)

        if (providedDelay == null) {
            internalState.value = internalState.value.copy(
                ready = CLOSED
            )
            close()
        } else {
            delay(duration = reconnectionTime ?: providedDelay)
            waitForInternetConnection()
            schedule(Connect)
        }
    }
}

private suspend fun SseEventSourceImpl.waitForInternetConnection() {
    val provider = isConnectedProvider
    if (provider != null) {
        provider()
            .filter { isConnected -> isConnected }
            .first()
    }
}
