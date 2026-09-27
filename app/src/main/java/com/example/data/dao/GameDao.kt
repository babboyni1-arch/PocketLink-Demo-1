package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY isFavorite DESC, lastPlayedTimestamp DESC, title ASC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM games WHERE packageName = :pkg LIMIT 1")
    suspend fun getGameByPackage(pkg: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("UPDATE games SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE games SET lastPlayedTimestamp = :time WHERE id = :id")
    suspend fun updateLastPlayed(id: Long, time: Long)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int
}
