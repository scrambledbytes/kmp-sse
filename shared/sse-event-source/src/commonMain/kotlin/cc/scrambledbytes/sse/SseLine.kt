package cc.scrambledbytes.sse

import kotlin.jvm.JvmInline

@JvmInline
value class SseLine(
    val value: String
) {
    val isDispatch: Boolean
        get() = value.isBlank()

    val isIgnore: Boolean
        get() = value.startsWith(":")

    val isProcessField: Boolean
        get() = ":" in value && !isIgnore
}
