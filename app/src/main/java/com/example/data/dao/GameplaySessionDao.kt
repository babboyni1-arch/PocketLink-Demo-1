package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GameplaySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameplaySessionDao {
    @Query("SELECT * FROM gameplay_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<GameplaySessionEntity>>

    @Query("SELECT * FROM gameplay_sessions WHERE gameId = :gameId ORDER BY startTimeMs DESC")
    fun getSessionsForGame(gameId: Long): Flow<List<GameplaySessionEntity>>

    @Query("SELECT * FROM gameplay_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): GameplaySessionEntity?

    @Query("SELECT * FROM gameplay_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): GameplaySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameplaySessionEntity): Long

    @Update
    suspend fun updateSession(session: GameplaySessionEntity)

    @Delete
    suspend fun deleteSession(session: GameplaySessionEntity)

    @Query("UPDATE gameplay_sessions SET notes = :notes, rating = :rating WHERE id = :id")
    suspend fun updateNotesAndRating(id: Long, notes: String, rating: Int)

    @Query("DELETE FROM gameplay_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
