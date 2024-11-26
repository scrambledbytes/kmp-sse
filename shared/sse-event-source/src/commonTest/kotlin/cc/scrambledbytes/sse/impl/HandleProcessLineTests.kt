package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import kotlin.test.BeforeTest
import kotlin.test.Test


class HandleProcessLineTests {
    lateinit var provider: FakeSseLineStreamProvider

    @BeforeTest
    fun setup() {
        provider = FakeSseLineStreamProvider()
    }

    @Test
    fun testSetEventType() {

    }

    @Test
    fun testSetData() {
        // append

    }

    @Test
    fun testSetId() {

    }

    @Test
    fun testIgnoreLine() {

    }

    @Test
    fun testRetry() {

    }


}