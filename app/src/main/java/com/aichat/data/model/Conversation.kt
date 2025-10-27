package com.aichat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val providerId: String,
    val model: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
