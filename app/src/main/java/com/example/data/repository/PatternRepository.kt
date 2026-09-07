package com.example.data.repository

import com.example.data.local.PatternDao
import com.example.data.local.PatternEntity
import com.example.data.model.BeadPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PatternRepository(
    private val patternDao: PatternDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        scope.launch {
            if (patternDao.getPatternCount() == 0) {
                val initialEntities = PresetPatterns.INITIAL_PATTERNS.map {
                    PatternEntity.fromDomain(it.copy(id = 0L))
                }
                patternDao.insertPatterns(initialEntities)
            }
        }
    }

    val allPatterns: Flow<List<BeadPattern>> = patternDao.getAllPatterns().map { list ->
        list.map { it.toDomain() }
    }

    val favoritePatterns: Flow<List<BeadPattern>> = patternDao.getFavoritePatterns().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getPatternById(id: Long): BeadPattern? {
        return patternDao.getPatternById(id)?.toDomain()
    }

    suspend fun savePattern(pattern: BeadPattern): Long {
        val entity = PatternEntity.fromDomain(pattern)
        return patternDao.insertPattern(entity)
    }

    suspend fun deletePatternById(id: Long) {
        patternDao.deletePatternById(id)
    }

    suspend fun toggleFavorite(id: Long, currentIsFavorite: Boolean) {
        patternDao.updateFavorite(id, !currentIsFavorite)
    }
}
