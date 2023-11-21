package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.LF
import cc.scrambledbytes.sse.SseEventSourceImpl
import cc.scrambledbytes.sse.SseLine
import kotlin.time.Duration.Companion.seconds

internal suspend fun SseEventSourceImpl.handleProcessLine(
    line: SseLine
) {
    when {
        line.isDispatch -> handleDispatch()
        line.isIgnore -> Unit // Ignore the line.
        line.isProcessField -> {
            val (name, value) = line.value.split(":", limit = 2)
            //If value starts with a U+0020 SPACE character, remove it from value.
            processField(
                name = name,
                fieldValue = value.removePrefix(" "),
            )
        }

        else -> processField(name = line.value, fieldValue = "")
    }
}

private fun SseEventSourceImpl.processField(
    name: String, fieldValue: String
) {
    when (name) {
        "event" -> handleEvent(fieldValue)
        "data" -> handleData(fieldValue)
        "id" -> handleId(fieldValue)
        "retry" -> handleRetry(fieldValue)
        else -> Unit // The field is ignored.
    }
}

/**
 * Set the event type buffer to field value.
 */
private fun SseEventSourceImpl.handleEvent(
    fieldValue: String
) {

    buffer = buffer.copy(eventType = fieldValue)
}

/**
 * Append the field value to the data buffer, then append a single U+000A LINE FEED (LF) character to the data buffer.
 */
private fun SseEventSourceImpl.handleData(
    fieldValue: String
) {
    val newData = buffer.data + fieldValue + LF
    buffer = buffer.copy(data = newData)
}

/*
 * If the field value does not contain U+0000 NULL, then set the last event ID buffer to the field value.
 *
 * Otherwise, ignore the field.
 */
private fun SseEventSourceImpl.handleId(
    fieldValue: String
) {
    if (Char.MIN_VALUE in fieldValue) {
        return
    }

    buffer = buffer.copy(lastEventId = fieldValue)
}

/**
 * If the field value consists of only ASCII digits, then interpret the field value as an integer in base ten, and
 * set the event stream's reconnection time to that integer.
 *
 * Otherwise, ignore the field.
 */
private fun SseEventSourceImpl.handleRetry(
    fieldValue: String
) {
    val newReconnectionTime = fieldValue.toIntOrNull(RECONNECTION_TIME)
        ?: return

    reconnectionTime = newReconnectionTime.seconds
}

private const val RECONNECTION_TIME = 10
