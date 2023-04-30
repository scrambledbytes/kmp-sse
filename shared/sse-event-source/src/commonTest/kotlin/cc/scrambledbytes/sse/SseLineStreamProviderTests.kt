package cc.scrambledbytes.sse

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

private const val VALID_URL = "https://test.com"
private const val INVALID_URL = "please crash"

/**
 * Template for testing a provider implementation.
 */
abstract class AbstractSseLineStreamProviderTests(
    open val provider: SseLineStream.Provider
) {

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


class RealTests:AbstractSseLineStreamProviderTests(FakeSseLineStreamProvider())