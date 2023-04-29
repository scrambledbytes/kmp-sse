package cc.scrambledbytes.sse

@JvmInline
value class SseLine(
    val value: String
) {
    // TODO validate UTF 8 encoding

    val isDispatch: Boolean
        get() = value.isBlank()

    val isIgnore: Boolean
        get() = value.startsWith(":")

    val isProcessField: Boolean
        get() = ":" in value
}