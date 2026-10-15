package io.github.reyfxck.wpsxandroid.core.process

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessArgumentPolicyTest {
    @Test
    fun rejectsBlankExecutableAndNul() {
        assertFalse(ProcessArgumentPolicy.validExecutable(""))
        assertFalse(ProcessArgumentPolicy.validExecutable("iw\u0000bad"))
        assertFalse(ProcessArgumentPolicy.validArgument("x\u0000y"))
    }

    @Test
    fun shellPunctuationIsJustLiteralArgvData() {
        assertTrue(ProcessArgumentPolicy.validExecutable("/system/bin/iw"))
        assertTrue(ProcessArgumentPolicy.validArgument("name;still-one-argument"))
    }
}
