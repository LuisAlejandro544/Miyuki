package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BeadPattern
import com.example.data.model.BeadTechnique

@Entity(tableName = "patterns")
data class PatternEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val technique: String, // LOOM, PEYOTE, BRICK_STITCH
    val columns: Int,
    val rows: Int,
    val gridData: String,
    val category: String,
    val difficulty: String,
    val isFavorite: Boolean,
    val isPreset: Boolean,
    val wristSizeCm: Float,
    val createdAt: Long
) {
    fun toDomain(): BeadPattern {
        val tech = try {
            BeadTechnique.valueOf(technique)
        } catch (e: Exception) {
            BeadTechnique.LOOM
        }
        val grid = BeadPattern.deserializeGrid(gridData, columns * rows)
        return BeadPattern(
            id = id,
            title = title,
            description = description,
            technique = tech,
            columns = columns,
            rows = rows,
            grid = grid,
            category = category,
            difficulty = difficulty,
            isFavorite = isFavorite,
            isPreset = isPreset,
            wristSizeCm = wristSizeCm,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: BeadPattern): PatternEntity {
            return PatternEntity(
                id = domain.id,
                title = domain.title,
                description = domain.description,
                technique = domain.technique.name,
                columns = domain.columns,
                rows = domain.rows,
                gridData = domain.serializeGrid(),
                category = domain.category,
                difficulty = domain.difficulty,
                isFavorite = domain.isFavorite,
                isPreset = domain.isPreset,
                wristSizeCm = domain.wristSizeCm,
                createdAt = domain.createdAt
            )
        }
    }
}
