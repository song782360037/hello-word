package com.aichat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aichat.data.model.WebDAVSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface WebDAVSettingsDao {
    @Query("SELECT * FROM webdav_settings WHERE id = 1")
    fun getSettings(): Flow<WebDAVSettings?>

    @Query("SELECT * FROM webdav_settings WHERE id = 1")
    suspend fun getSettingsOnce(): WebDAVSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: WebDAVSettings)

    @Update
    suspend fun update(settings: WebDAVSettings)
}
