package cc.scrambledbytes.sse

data class SseEvent(
    val type: String,
    val data: String,
    val origin: String, // URL after redirects
    val lastEventId: String,
)