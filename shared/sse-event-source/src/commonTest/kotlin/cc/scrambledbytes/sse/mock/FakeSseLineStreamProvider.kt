package cc.scrambledbytes.sse.mock

import cc.scrambledbytes.sse.SseLine
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.SseUrl

class FakeSseLineStreamProvider : SseLineStream.Provider {
    var onCloseVisited: Boolean = false
    var onExecuteVisited: Boolean = false
    var parseVisited: Boolean = false
    var parseVisitedWith: SseUrl? = null
    var connectionState: SseLineStream.ConnectionState = initialState

    fun onClose() {
        onCloseVisited = true
    }

    lateinit var postLine: suspend (SseLine) -> Unit

    suspend fun onExecute(
        onState: suspend (SseLineStream.ConnectionState) -> Unit,
        onLine: suspend (SseLine) -> Unit,
    ) {
        onExecuteVisited = true

        onState(connectionState)

        this.postLine = onLine
    }

    override suspend fun create(
        url: SseUrl,
        lastEventId: String?,
        withCredentials: Boolean
    ): SseLineStream =
        SseLineStream(
            onClose = this::onClose,
            onExecute = this::onExecute,
        )

    override fun parse(
        url: String
    ): SseUrl {
        parseVisited = true
        parseVisitedWith = SseUrl(url)
        require(url.startsWith("https"))
        return SseUrl(url)
    }

    companion object {
        val initialState = SseLineStream.ConnectionState(-1, "", false)
    }
}