package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import org.junit.Before
import org.junit.Test

class HandleErrorTests {
    lateinit var provider: FakeSseLineStreamProvider

    @Before
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testIgnoreCancellationException() {

    }

    @Test
    fun testCloseWhenFail() {

    }

    @Test
    fun testCloseWhenCustomFail() {

    }

    @Test
    fun testReconnectOnError() {

    }

    @Test
    fun testAbortOnFail() {

    }
}