package cc.scrambledbytes.sse.client

import cc.scrambledbytes.sse.SseEventSource

internal suspend fun SseEventSource.processLine(line: String) {
        when {
            line.isBlank() -> dispatchEvent()
            line.startsWith(":") -> Unit // Ignore the line.
            ":" in line -> {
                val (name, value) = line.split(":")
                processField(
                    name = name,
                    value.removePrefix(" "), //If value starts with a U+0020 SPACE character, remove it from value.
                )
            }
            else -> processField(name = line, fieldValue = "")
        }
    }