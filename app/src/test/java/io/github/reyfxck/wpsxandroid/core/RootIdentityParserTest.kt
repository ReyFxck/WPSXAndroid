package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.capability.RootIdentityParser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RootIdentityParserTest {
    @Test
    fun acceptsUidZeroFromIdOutput() {
        assertTrue(RootIdentityParser.isRootIdentity("uid=0(root) gid=0(root) groups=0(root)"))
    }

    @Test
    fun rejectsNormalAppUid() {
        assertFalse(RootIdentityParser.isRootIdentity("uid=10234(u0_a234) gid=10234(u0_a234)"))
    }

    @Test
    fun doesNotConfuseEmbeddedTextWithUidField() {
        assertFalse(RootIdentityParser.isRootIdentity("euid=0(root) uid=2000(shell)"))
    }
}
