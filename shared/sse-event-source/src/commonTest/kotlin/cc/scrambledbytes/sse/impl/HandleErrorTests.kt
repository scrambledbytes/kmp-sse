package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import kotlin.test.BeforeTest
import kotlin.test.Test


class HandleErrorTests {
    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
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