package io.github.reyfxck.wpsxandroid.core.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalDistanceTest {
    @Test
    fun oneMeterReferenceIsOneMeter() {
        assertEquals(1.0, estimateDistanceMeters(-40), 0.0001)
    }

    @Test
    fun weakerSignalProducesLargerDistanceEstimate() {
        val near = estimateDistanceMeters(-50)
        val far = estimateDistanceMeters(-75)

        assertTrue(far > near)
    }
}
