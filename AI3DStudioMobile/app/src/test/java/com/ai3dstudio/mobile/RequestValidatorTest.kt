package com.ai3dstudio.mobile

import com.ai3dstudio.mobile.core.security.RequestValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestValidatorTest {

    @Test
    fun `valid https base url is accepted`() {
        assertTrue(RequestValidator.isValidBaseUrl("https://example.com/api"))
    }

    @Test
    fun `http base url is rejected`() {
        assertFalse(RequestValidator.isValidBaseUrl("http://example.com/api"))
    }

    @Test
    fun `blank base url is rejected`() {
        assertFalse(RequestValidator.isValidBaseUrl(""))
    }

    @Test
    fun `prompt sanitization trims and strips control characters`() {
        val sanitized = RequestValidator.sanitizePrompt("  hello\u0000world  ")
        assertEquals("helloworld", sanitized)
    }

    @Test
    fun `empty prompt is invalid`() {
        assertFalse(RequestValidator.isValidPrompt(""))
    }

    @Test
    fun `non-empty prompt within limit is valid`() {
        assertTrue(RequestValidator.isValidPrompt("أنشئ سيارة رياضية"))
    }
}
