package com.ai3dstudio.mobile.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the user's server Base URL and any derived session tokens using
 * Android Keystore backed AES256-GCM encryption via EncryptedSharedPreferences.
 * No plaintext secret ever touches disk.
 */
@Singleton
class SecureCredentialStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveBaseUrl(baseUrl: String) {
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply()
    }

    fun getBaseUrl(): String? = prefs.getString(KEY_BASE_URL, null)

    fun saveCertificatePin(pin: String) {
        prefs.edit().putString(KEY_CERT_PIN, pin).apply()
    }

    fun getCertificatePin(): String? = prefs.getString(KEY_CERT_PIN, null)

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val SECURE_PREFS_FILE = "secure_prefs"
        const val KEY_BASE_URL = "server_base_url"
        const val KEY_CERT_PIN = "server_cert_pin"
    }
}
