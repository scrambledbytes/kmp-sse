package cc.scrambledbytes.sse.util

fun Any.debugTrace(message:String) {
    println("[${this::class.simpleName} - ($this)] $message")
}
