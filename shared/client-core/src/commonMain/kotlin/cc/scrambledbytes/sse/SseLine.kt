package cc.scrambledbytes.sse

@JvmInline
value class SseLine(
    val value: String
) {
    val isDispatch: Boolean
        get() = value.isBlank()

    val isIgnore: Boolean
        get() = value.startsWith(":")

    val isProcessField: Boolean
        get() = ":" in value
}