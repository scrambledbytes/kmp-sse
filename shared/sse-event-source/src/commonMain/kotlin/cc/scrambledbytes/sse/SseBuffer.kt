package cc.scrambledbytes.sse

internal data class SseBuffer(
    val eventType: String = "",
    val data: String = "", // data buffer
    val lastEventId: String? = null, // id
) {
    val isEmpty: Boolean by lazy {
        data.isBlank()
    }

    fun toSseEvent(): SseEvent =
        SseEvent(
            data = data.removeSuffix(LF.toString()),
            lastEventId = lastEventId,
            type = eventType,
        )
}
