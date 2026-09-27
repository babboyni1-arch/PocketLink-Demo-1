package com.example

import android.app.Application
import com.example.data.database.PocketLinkDatabase
import com.example.data.repository.GameRepository
import com.example.data.repository.GameplaySessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketLinkApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { PocketLinkDatabase.getDatabase(this) }
    val gameRepository by lazy { GameRepository(database.gameDao(), this) }
    val gameplaySessionRepository by lazy { GameplaySessionRepository(database.gameplaySessionDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            gameRepository.seedDefaultGamesIfEmpty()
        }
    }
}
