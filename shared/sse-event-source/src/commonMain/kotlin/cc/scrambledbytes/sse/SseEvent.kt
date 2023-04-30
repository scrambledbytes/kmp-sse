package cc.scrambledbytes.sse

data class SseEvent(
    val type: String,
    val data: String,
    val lastEventId: String,
)