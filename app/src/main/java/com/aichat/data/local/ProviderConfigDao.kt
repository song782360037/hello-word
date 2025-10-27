package com.aichat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aichat.data.model.ProviderConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs")
    fun getAllConfigs(): Flow<List<ProviderConfig>>

    @Query("SELECT * FROM provider_configs WHERE id = :providerId")
    suspend fun getConfig(providerId: String): ProviderConfig?

    @Query("SELECT * FROM provider_configs WHERE id = :providerId")
    fun getConfigFlow(providerId: String): Flow<ProviderConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ProviderConfig)

    @Update
    suspend fun update(config: ProviderConfig)

    @Query("DELETE FROM provider_configs WHERE id = :providerId")
    suspend fun delete(providerId: String)
}
