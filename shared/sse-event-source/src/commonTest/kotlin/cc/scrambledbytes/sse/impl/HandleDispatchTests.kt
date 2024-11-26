package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import kotlin.test.BeforeTest
import kotlin.test.Test

class HandleDispatchTests {
    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
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

