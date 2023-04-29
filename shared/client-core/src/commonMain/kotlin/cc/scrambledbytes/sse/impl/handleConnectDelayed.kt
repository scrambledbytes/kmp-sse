package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.ReadyState
import cc.scrambledbytes.sse.ReadyState.CONNECTING
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseEventSourceImpl.Intent.Connect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun SseEventSourceImpl.handleDelayedConnectionAttempt() {
    _state.value = _state.value.copy(
        ready = CONNECTING
    )

    lineScope.launch {
        connectionAttempt++

        delay(duration = reconnectionTime ?: delayProvider(connectionAttempt))

        /*
        TODO Optionally, wait some more. In particular, if the previous attempt failed, then user agents might introduce an exponential backoff delay to avoid overloading a potentially already overloaded server. Alternatively, if the operating system has reported that there is no network connectivity, user agents might wait for the operating system to announce that the network connection has returned before retrying.
        */

        schedule(Connect)
    }
}
