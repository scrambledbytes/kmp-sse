package cc.scrambledbytes.sse.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun launchOnDefault(
    block: suspend () -> Unit
) {
    coroutineScope {
        launch(CoroutineScope(Dispatchers.Default).coroutineContext) {
            block()
        }
    }
}