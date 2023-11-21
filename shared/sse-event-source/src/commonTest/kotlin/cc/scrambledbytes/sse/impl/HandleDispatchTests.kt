package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import org.junit.Before
import org.junit.Test

class HandleDispatchTests {
    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testDontDispatchEmpty() {

    }

    @Test
    fun testWhenSetReconnectIdWhenReconnecting() {

    }

    @Test
    fun testDispatchClearsBuffer() {

    }

    @Test
    fun testDispatchPublishesEvent() {

    }
}

