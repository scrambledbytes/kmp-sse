package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal const val VALID_URL = "https://test.com"
internal const val INVALID_URL = "please crash"

/**
 * Template for testing a provider implementation.
 */
abstract class AbstractSseLineStreamProviderTests(
    val url: String,
    open val provider: SseLineStream.Provider
) {
    lateinit var source: SseEventSource

    @BeforeTest
    fun setup() {
        source = SseEventSource(url, provider)
    }

    @Test
    fun testParseValid() {
        assertEquals(provider.parse(VALID_URL), SseUrl(VALID_URL))
    }

    @Test
    fun testParseInvalid() {
        assertFails {
            provider.parse(INVALID_URL)
        }
    }

}

class RealTests : AbstractSseLineStreamProviderTests(
    provider = FakeSseLineStreamProvider(),
    url = VALID_URL
)