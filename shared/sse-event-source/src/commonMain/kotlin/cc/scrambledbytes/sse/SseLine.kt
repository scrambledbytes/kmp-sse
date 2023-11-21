@file:Suppress("UndocumentedPublicProperty")

package cc.scrambledbytes.sse

import kotlin.jvm.JvmInline

/**
 * An incoming SSE line.
 */
@JvmInline
value class SseLine(
    val value: String
) {
    /**
     *  Whether the SSE Line is a dispatch command.
     */
    val isDispatch: Boolean
        get() = value.isBlank()

    /**
     * Whether the SSE Line should be ignored.
     */
    val isIgnore: Boolean
        get() = value.startsWith(":")

    /**
     * Whether the SSE Line is a field with value.
     */
    val isProcessField: Boolean
        get() = ":" in value && !isIgnore
}
