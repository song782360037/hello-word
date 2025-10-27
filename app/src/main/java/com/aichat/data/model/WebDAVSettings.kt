package com.aichat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webdav_settings")
data class WebDAVSettings(
    @PrimaryKey
    val id: Int = 1,
    val url: String,
    val username: String,
    val passwordEnc: String,
    val remotePath: String,
    val includeApiKeys: Boolean = false,
    val lastSyncAt: Long = 0
)
