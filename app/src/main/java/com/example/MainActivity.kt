package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.GameEntity
import com.example.data.model.GameplaySessionEntity
import com.example.ui.screens.GameLibraryScreen
import com.example.ui.screens.GameplayDetailScreen
import com.example.ui.screens.GameplayHistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostSessionScreen
import com.example.ui.screens.LiveUsageScreen
import com.example.ui.screens.RemotePlayScreen
import com.example.ui.screens.SpectatorScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PocketLinkViewModel

enum class PocketScreen {
    HOME,
    GAME_LIBRARY,
    LIVE_USAGE,
    HOST_SESSION,
    REMOTE_PLAY,
    SPECTATOR,
    GAMEPLAY_HISTORY,
    GAMEPLAY_DETAIL
}

class MainActivity : ComponentActivity() {

    private val viewModel: PocketLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                RequestPermissions()

                var currentScreen by remember { mutableStateOf(PocketScreen.HOME) }
                var selectedSessionForDetail by remember { mutableStateOf<GameplaySessionEntity?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBackground
                ) {
                    when (currentScreen) {
                        PocketScreen.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToLibrary = { currentScreen = PocketScreen.GAME_LIBRARY },
                                onNavigateToLiveWatch = { currentScreen = PocketScreen.LIVE_USAGE },
                                onNavigateToHost = { currentScreen = PocketScreen.HOST_SESSION },
                                onNavigateToRemotePlay = { currentScreen = PocketScreen.REMOTE_PLAY },
                                onNavigateToHistory = { currentScreen = PocketScreen.GAMEPLAY_HISTORY },
                                onWatchGame = { usage ->
                                    viewModel.startWatchingGame(usage) { success ->
                                        if (success) currentScreen = PocketScreen.SPECTATOR
                                    }
                                }
                            )
                        }

                        PocketScreen.GAME_LIBRARY -> {
                            BackHandler { currentScreen = PocketScreen.HOME }
                            GameLibraryScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = PocketScreen.HOME },
                                onHostGame = { game ->
                                    viewModel.selectGameForHosting(game)
                                    currentScreen = PocketScreen.HOST_SESSION
                                }
                            )
                        }

                        PocketScreen.LIVE_USAGE -> {
                            BackHandler { currentScreen = PocketScreen.HOME }
                            LiveUsageScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = PocketScreen.HOME },
                                onWatchLive = { gameUsage ->
                                    viewModel.startWatchingGame(gameUsage) { success ->
                                        if (success) currentScreen = PocketScreen.SPECTATOR
                                    }
                                }
                            )
                        }

                        PocketScreen.HOST_SESSION -> {
                            BackHandler { currentScreen = PocketScreen.HOME }
                            HostSessionScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = PocketScreen.HOME },
                                onSessionFinalized = {
                                    currentScreen = PocketScreen.GAMEPLAY_HISTORY
                                }
                            )
                        }

                        PocketScreen.REMOTE_PLAY -> {
                            BackHandler { currentScreen = PocketScreen.HOME }
                            RemotePlayScreen(
                                viewModel = viewModel,
                                onDisconnect = { currentScreen = PocketScreen.HOME }
                            )
                        }

                        PocketScreen.SPECTATOR -> {
                            BackHandler { currentScreen = PocketScreen.LIVE_USAGE }
                            SpectatorScreen(
                                viewModel = viewModel,
                                onExitWatch = { currentScreen = PocketScreen.LIVE_USAGE }
                            )
                        }

                        PocketScreen.GAMEPLAY_HISTORY -> {
                            BackHandler { currentScreen = PocketScreen.HOME }
                            GameplayHistoryScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = PocketScreen.HOME },
                                onSelectSession = { session ->
                                    selectedSessionForDetail = session
                                    currentScreen = PocketScreen.GAMEPLAY_DETAIL
                                }
                            )
                        }

                        PocketScreen.GAMEPLAY_DETAIL -> {
                            BackHandler { currentScreen = PocketScreen.GAMEPLAY_HISTORY }
                            selectedSessionForDetail?.let { session ->
                                GameplayDetailScreen(
                                    session = session,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = PocketScreen.GAMEPLAY_HISTORY }
                                )
                            } ?: run {
                                currentScreen = PocketScreen.GAMEPLAY_HISTORY
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestPermissions() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        perms.add(Manifest.permission.RECORD_AUDIO)
        permissionLauncher.launch(perms.toTypedArray())
    }
}
