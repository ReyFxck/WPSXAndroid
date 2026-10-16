package io.github.reyfxck.wpsxandroid.core.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitorBackendClassifierTest {
    @Test
    fun `existing monitor interface wins over generic support`() {
        val candidates = MonitorBackendClassifier.classify(
            existingMonitorInterfaces = listOf("mon0"),
            genericMonitorAdvertised = true,
        )

        val selected = MonitorBackendSelector.select(candidates)
        assertEquals("existing-monitor-interface", selected?.id)
        assertEquals(MonitorInterfaceStrategy.EXISTING_MONITOR_INTERFACE, selected?.strategy)
    }

    @Test
    fun `same interface hint is preserved when generic support is unknown`() {
        val candidates = MonitorBackendClassifier.classify(
            existingMonitorInterfaces = emptyList(),
            genericMonitorAdvertised = null,
            sameInterfaceHint = "Driver exposes a same-interface monitor path.",
        )

        val selected = MonitorBackendSelector.select(candidates)
        assertEquals("same-interface-hint", selected?.id)
        assertEquals(CapabilityState.UNKNOWN, selected?.state)
        assertTrue(selected?.detail?.contains("same-interface") == true)
    }

    @Test
    fun `generic available backend is selected when no stronger evidence exists`() {
        val candidates = MonitorBackendClassifier.classify(
            existingMonitorInterfaces = emptyList(),
            genericMonitorAdvertised = true,
        )

        val selected = MonitorBackendSelector.select(candidates)
        assertEquals("generic-nl80211", selected?.id)
        assertEquals(CapabilityState.AVAILABLE, selected?.state)
    }
}
