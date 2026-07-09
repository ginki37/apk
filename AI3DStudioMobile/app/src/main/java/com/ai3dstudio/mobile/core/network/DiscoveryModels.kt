package com.ai3dstudio.mobile.core.network

import kotlinx.serialization.Serializable

/**
 * Generic capability-discovery contract. The client probes the user's
 * Base URL for a small set of well-known discovery endpoints
 * (/models, /capabilities, /v1/models, /health, /limits) and merges
 * whatever the server actually exposes. No field is required — every
 * discovered capability degrades gracefully if the server omits it.
 */
@Serializable
data class ServerInfo(
    val name: String? = null,
    val version: String? = null,
    val provider: String? = null
)

@Serializable
data class ModelInfo(
    val id: String,
    val displayName: String? = null,
    val supportsChat: Boolean = false,
    val supportsImage: Boolean = false,
    val supportsModel3d: Boolean = false,
    val supportsGame: Boolean = false
)

@Serializable
data class RateLimitInfo(
    val requestsPerMinute: Int? = null,
    val requestsPerDay: Int? = null,
    val remaining: Int? = null
)

@Serializable
data class CapabilityManifest(
    val server: ServerInfo = ServerInfo(),
    val models: List<ModelInfo> = emptyList(),
    val chatCapable: Boolean = false,
    val imageCapable: Boolean = false,
    val model3dCapable: Boolean = false,
    val gameCapable: Boolean = false,
    val rateLimit: RateLimitInfo = RateLimitInfo()
) {
    val hasAnyCapability: Boolean
        get() = chatCapable || imageCapable || model3dCapable || gameCapable
}
