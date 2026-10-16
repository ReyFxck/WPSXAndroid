package io.github.reyfxck.wpsxandroid.core.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacVendorResolverTest {
    @Test
    fun `resolves known prefixes regardless of separators`() {
        assertEquals("Intelbras", MacVendorResolver.resolve("54:6c:ac:06:9a:34"))
        assertEquals("Huawei", MacVendorResolver.resolve("D4-4F-67-00-00-00"))
        assertEquals("Arcadyan", MacVendorResolver.resolve("84A329112233"))
    }

    @Test
    fun `unknown or malformed addresses return null`() {
        assertNull(MacVendorResolver.resolve("00:00:00:00:00:00"))
        assertNull(MacVendorResolver.resolve("bad"))
    }
}
