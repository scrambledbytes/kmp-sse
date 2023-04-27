package cc.scrambledbytes.sse

import com.sun.tools.javac.util.LayoutCharacters.LF

internal data class SseBuffer(
    val eventType: String = "",
    val lastEventId: String = "", // id
    val data: String = "", // data buffer
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
