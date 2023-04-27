package cc.scrambledbytes.sse.client

import cc.scrambledbytes.sse.LF
import cc.scrambledbytes.sse.SseClient
import kotlin.time.Duration.Companion.milliseconds

internal fun SseClient.processField(
    name: String, fieldValue: String) {
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
internal fun SseClient.handleEvent(
    fieldValue: String
) {
    buffer = buffer.copy(eventType = fieldValue)
}

/**
 * Append the field value to the data buffer, then append a single U+000A LINE FEED (LF) character to the data buffer.
 */
internal fun SseClient.handleData(
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
internal fun SseClient.handleId(
    fieldValue: String
) {
    if (Character.MIN_VALUE in fieldValue)
        return

    buffer = buffer.copy(lastEventId = fieldValue)
}

/**
 * If the field value consists of only ASCII digits, then interpret the field value as an integer in base ten, and
 * set the event stream's reconnection time to that integer.
 *
 * Otherwise, ignore the field.
 */
internal fun SseClient.handleRetry(
    fieldValue: String
) {
    val newReconnectionTime = fieldValue.toIntOrNull(10)
        ?: return

    reconnectionTime = newReconnectionTime.milliseconds
}