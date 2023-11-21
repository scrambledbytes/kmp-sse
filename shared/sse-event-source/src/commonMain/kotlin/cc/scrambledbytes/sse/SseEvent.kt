package cc.scrambledbytes.sse

/**
 * An SseEvent.
 */
data class SseEvent(
    val type: String,
    val data: String,
    val lastEventId: String?,
)
