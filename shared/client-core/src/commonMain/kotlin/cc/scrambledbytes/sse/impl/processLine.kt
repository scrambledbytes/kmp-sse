package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLine

internal suspend fun SseEventSourceImpl.processLine(
    line: SseLine
) {
    when {
        line.isDispatch -> dispatchEvent()
        line.isIgnore -> Unit // Ignore the line.
        line.isProcessField -> {
            val (name, value) = line.value.split(":")
            processField(
                name = name,
                value.removePrefix(" "), //If value starts with a U+0020 SPACE character, remove it from value.
            )
        }

        else -> processField(name = line.value, fieldValue = "")
    }
}