package com.ai3dstudio.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_profiles")
data class ServerProfileEntity(
    @PrimaryKey val baseUrl: String,
    val serverName: String?,
    val serverVersion: String?,
    val capabilitiesJson: String,
    val lastDiscoveredAt: Long,
    val isActive: Boolean
)
