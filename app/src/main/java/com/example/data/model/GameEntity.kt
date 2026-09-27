package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val packageName: String,
    val category: String = "Action",
    val accentColorHex: String = "#06B6D4",
    val isCustom: Boolean = false,
    val targetFps: Int = 60,
    val targetResolution: String = "720p",
    val preferredControllerLayout: String = "Standard",
    val lastPlayedTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val isSupported: Boolean = true,
    val supportStatusNote: String = "Full Screen & Remote Input Supported"
)
