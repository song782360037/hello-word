package com.aichat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aichat.data.model.Conversation
import com.aichat.data.model.Message
import com.aichat.data.model.ProviderConfig
import com.aichat.data.model.WebDAVSettings

@Database(
    entities = [
        ProviderConfig::class,
        Conversation::class,
        Message::class,
        WebDAVSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun webDAVSettingsDao(): WebDAVSettingsDao
}
