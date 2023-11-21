package cc.scrambledbytes.sse.mock

import cc.scrambledbytes.sse.SseLine
import cc.scrambledbytes.sse.SseLineStream
import cc.scrambledbytes.sse.SseUrl
import cc.scrambledbytes.sse.util.debugTrace

class FakeSseLineStreamProvider : SseLineStream.Provider {
    var createVisitedWithLastEventId: String? = null
    var createVisitedWithUrl: SseUrl? = null
    var createVisitedWithCredentials: Boolean? = null
    var createVisited: Boolean = false
    var onCloseVisited: Boolean = false
    var onExecuteVisited: Boolean = false
    var parseVisited: Boolean = false
    var parseVisitedWith: SseUrl? = null
    var connectionState: SseLineStream.ConnectionState = initialState

    fun onClose() {
        debugTrace("onClose")
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
        withCredentials: Boolean,
        extraHeaders: Map<String, String>
    ): SseLineStream {
        createVisited = true
        createVisitedWithUrl = url
        createVisitedWithLastEventId = lastEventId
        createVisitedWithCredentials = withCredentials
        val stream = SseLineStream(
            onClose = this::onClose,
            onConnect = this::onExecute,
        )
        return stream
    }

    override fun parse(
        url: String
    ): SseUrl {
        parseVisited = true
        parseVisitedWith = SseUrl(url)
        require(url.startsWith("https"))
        return SseUrl(url)
    }

    companion object {
        val initialState = SseLineStream.ConnectionState(
            statusCode = -1,
            contentType = "",
            isAborted = false
        )
    }
}
