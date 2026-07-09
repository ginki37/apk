package com.ai3dstudio.mobile.core.security

import okhttp3.CertificatePinner
import java.security.MessageDigest
import java.security.cert.Certificate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds an OkHttp CertificatePinner dynamically for the host the user
 * configured via Base URL. Pins are captured (TOFU: trust-on-first-use) on
 * the first successful connection and persisted, then enforced afterwards.
 */
@Singleton
class DynamicCertificatePinner @Inject constructor(
    private val credentialStore: SecureCredentialStore
) {
    fun buildFor(host: String): CertificatePinner {
        val storedPin = credentialStore.getCertificatePin()
        val builder = CertificatePinner.Builder()
        if (storedPin != null) {
            builder.add(host, storedPin)
        }
        return builder.build()
    }

    fun capturePin(host: String, certificates: List<Certificate>) {
        val leaf = certificates.firstOrNull() ?: return
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(leaf.publicKey.encoded)
        val pin = "sha256/" + android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        credentialStore.saveCertificatePin(pin)
    }
}
