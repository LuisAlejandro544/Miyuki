package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllPatterns(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE id = :id LIMIT 1")
    suspend fun getPatternById(id: Long): PatternEntity?

    @Query("SELECT * FROM patterns WHERE technique = :technique ORDER BY createdAt DESC")
    fun getPatternsByTechnique(technique: String): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePatterns(): Flow<List<PatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternEntity>)

    @Update
    suspend fun updatePattern(pattern: PatternEntity)

    @Query("UPDATE patterns SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun deletePattern(pattern: PatternEntity)

    @Query("DELETE FROM patterns WHERE id = :id")
    suspend fun deletePatternById(id: Long)

    @Query("SELECT COUNT(*) FROM patterns")
    suspend fun getPatternCount(): Int
}
