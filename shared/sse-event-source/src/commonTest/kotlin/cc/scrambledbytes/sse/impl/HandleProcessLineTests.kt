package cc.scrambledbytes.sse.impl

import cc.scrambledbytes.sse.mock.FakeSseLineStreamProvider
import org.junit.Before
import org.junit.Test

class HandleProcessLineTests {
    lateinit var provider: FakeSseLineStreamProvider

    @Before
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