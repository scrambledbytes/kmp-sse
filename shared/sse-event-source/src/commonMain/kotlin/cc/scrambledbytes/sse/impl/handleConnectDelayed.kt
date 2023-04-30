package cc.scrambledbytes.sse.impl


import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventSourceImpl.Intent.Connect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal fun SseEventSourceImpl.handleDelayedConnectionAttempt() {
    _state.value = _state.value.copy(
        ready = CONNECTING
    )

    lineScope.launch {
        connectionAttempt++

        delay(duration = reconnectionTime ?: delayProvider(connectionAttempt))

        waitForInternetConnection()

        schedule(Connect)
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
