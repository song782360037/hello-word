package com.aichat.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CryptoUtil(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun encrypt(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun decrypt(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    companion object {
        fun encryptionKey(providerId: String): String = "api_key_$providerId"
        fun webdavPasswordKey(): String = "webdav_password"
    }
}
