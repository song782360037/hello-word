package com.aichat

import android.app.Application
import androidx.room.Room
import com.aichat.data.local.AppDatabase
import com.aichat.data.repository.ChatRepository
import com.aichat.data.repository.WebDAVRepository
import com.aichat.util.CryptoUtil

class AIChatApplication : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var cryptoUtil: CryptoUtil
        private set

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var webdavRepository: WebDAVRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aichat_database"
        ).build()

        cryptoUtil = CryptoUtil(applicationContext)

        chatRepository = ChatRepository(
            providerConfigDao = database.providerConfigDao(),
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao(),
            cryptoUtil = cryptoUtil
        )

        webdavRepository = WebDAVRepository(
            webDAVSettingsDao = database.webDAVSettingsDao(),
            providerConfigDao = database.providerConfigDao(),
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao(),
            cryptoUtil = cryptoUtil
        )
    }
}
