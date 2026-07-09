package com.ai3dstudio.mobile.core.security

import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client-side request rate limiter and validator. Prevents runaway request
 * loops (e.g. 3D generation retries) from overwhelming the user's own
 * backend, and rejects obviously malformed requests before they leave the
 * device.
 */
@Singleton
class RequestRateLimiter @Inject constructor() {

    private val concurrencyGate = Semaphore(permits = MAX_CONCURRENT_REQUESTS)
    private val recentRequestTimestamps = ConcurrentLinkedDeque<Long>()

    suspend fun <T> withRateLimit(block: suspend () -> T): T {
        pruneOldTimestamps()
        check(recentRequestTimestamps.size < MAX_REQUESTS_PER_WINDOW) {
            "تم تجاوز الحد الأقصى للطلبات، الرجاء المحاولة بعد قليل"
        }
        concurrencyGate.acquire()
        try {
            recentRequestTimestamps.addLast(System.currentTimeMillis())
            return block()
        } finally {
            concurrencyGate.release()
        }
    }

    private fun pruneOldTimestamps() {
        val cutoff = System.currentTimeMillis() - WINDOW_MILLIS
        while (recentRequestTimestamps.peekFirst()?.let { it < cutoff } == true) {
            recentRequestTimestamps.pollFirst()
        }
    }

    companion object {
        private const val MAX_CONCURRENT_REQUESTS = 4
        private const val MAX_REQUESTS_PER_WINDOW = 30
        private const val WINDOW_MILLIS = 60_000L
    }
}

object RequestValidator {
    private val urlRegex = Regex("^https://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$")

    fun isValidBaseUrl(url: String): Boolean = urlRegex.matches(url.trim())

    fun sanitizePrompt(prompt: String): String =
        prompt.trim().take(4000).replace(Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]"), "")

    fun isValidPrompt(prompt: String): Boolean =
        prompt.isNotBlank() && prompt.length in 1..4000
}
