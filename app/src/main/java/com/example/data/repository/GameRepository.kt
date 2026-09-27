package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.dao.GameDao
import com.example.data.model.GameEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val gameDao: GameDao,
    private val context: Context
) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun getGameById(id: Long): GameEntity? = gameDao.getGameById(id)

    suspend fun addGame(game: GameEntity): Long = gameDao.insertGame(game)

    suspend fun updateGame(game: GameEntity) = gameDao.updateGame(game)

    suspend fun deleteGame(game: GameEntity) = gameDao.deleteGame(game)

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = gameDao.toggleFavorite(id, isFav)

    suspend fun updateLastPlayed(id: Long) = gameDao.updateLastPlayed(id, System.currentTimeMillis())

    suspend fun seedDefaultGamesIfEmpty() = withContext(Dispatchers.IO) {
        if (gameDao.getGameCount() == 0) {
            val defaults = listOf(
                GameEntity(
                    title = "Minecraft",
                    packageName = "com.mojang.minecraftpe",
                    category = "Sandbox",
                    accentColorHex = "#10B981",
                    targetFps = 60,
                    targetResolution = "1080p",
                    preferredControllerLayout = "FPS",
                    supportStatusNote = "Full Touch & Virtual Pad Support"
                ),
                GameEntity(
                    title = "Roblox",
                    packageName = "com.roblox.client",
                    category = "Adventure",
                    accentColorHex = "#EF4444",
                    targetFps = 60,
                    targetResolution = "720p",
                    preferredControllerLayout = "Standard",
                    supportStatusNote = "Optimized for Virtual Joysticks"
                ),
                GameEntity(
                    title = "Genshin Impact",
                    packageName = "com.miHoYo.GenshinImpact",
                    category = "RPG",
                    accentColorHex = "#8B5CF6",
                    targetFps = 60,
                    targetResolution = "1080p",
                    preferredControllerLayout = "RPG",
                    supportStatusNote = "High Bitrate Profile Recommended"
                ),
                GameEntity(
                    title = "Asphalt 9: Legends",
                    packageName = "com.gameloft.android.ANMP.GloftA9HM",
                    category = "Racing",
                    accentColorHex = "#F59E0B",
                    targetFps = 60,
                    targetResolution = "1080p",
                    preferredControllerLayout = "Racing",
                    supportStatusNote = "Low Latency Tilt & Steering Ready"
                ),
                GameEntity(
                    title = "Retro Game Emulator",
                    packageName = "com.retroarch",
                    category = "Retro / Arcade",
                    accentColorHex = "#06B6D4",
                    targetFps = 60,
                    targetResolution = "720p",
                    preferredControllerLayout = "Platformer",
                    supportStatusNote = "D-Pad & 6-Button Arcade Map"
                )
            )
            gameDao.insertGames(defaults)
        }
    }

    suspend fun scanInstalledApps(): List<GameEntity> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val discovered = mutableListOf<GameEntity>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            // Skip own package
            if (pkg == context.packageName) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val appInfo = resolveInfo.activityInfo.applicationInfo

            val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                        (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            } else {
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            discovered.add(
                GameEntity(
                    title = appName,
                    packageName = pkg,
                    category = if (isGame) "Game" else "App",
                    accentColorHex = if (isGame) "#06B6D4" else "#8B5CF6",
                    isCustom = true,
                    targetFps = 60,
                    targetResolution = "720p",
                    preferredControllerLayout = "Standard",
                    supportStatusNote = if (isGame) "Direct Game Stream Ready" else "Standard Screen & Touch Stream"
                )
            )
        }
        discovered
    }
}
