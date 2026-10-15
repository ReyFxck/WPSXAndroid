package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.wps.Confidence
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class WpsPinContractTest {
    @Test
    fun candidateKeepsEightDigitPinVerbatim() {
        val candidate = WpsPinCandidate(
            pin = "01234567",
            algorithmId = "test-vector",
            confidence = Confidence.EXACT,
        )

        assertEquals("01234567", candidate.pin)
    }
}
