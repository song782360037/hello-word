package com.aichat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aichat.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversation(conversationId: String): Conversation?

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationFlow(conversationId: String): Flow<Conversation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: Conversation)

    @Update
    suspend fun update(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun delete(conversationId: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
