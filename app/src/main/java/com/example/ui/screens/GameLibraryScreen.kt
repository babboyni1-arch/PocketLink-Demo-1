package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GameEntity
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.viewmodel.PocketLinkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLibraryScreen(
    viewModel: PocketLinkViewModel,
    onBack: () -> Unit,
    onHostGame: (GameEntity) -> Unit
) {
    val games by viewModel.allGames.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Sandbox", "Action", "RPG", "Racing", "Custom")

    val filteredGames = games.filter { game ->
        val matchesCategory = selectedCategory == "All" || game.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = game.title.contains(searchQuery, ignoreCase = true) ||
                game.packageName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GAME LIBRARY",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "Add and configure games for remote play",
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.scanInstalledGames() },
                        modifier = Modifier.testTag("scan_games_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Installed Games",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonCyan,
                contentColor = CyberBackground,
                modifier = Modifier.testTag("add_game_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Game")
            }
        },
        containerColor = CyberBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_game_field"),
                placeholder = { Text("Search games or packages...", color = CyberTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyberTextMuted)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface,
                    focusedTextColor = CyberTextPrimary,
                    unfocusedTextColor = CyberTextPrimary
                )
            )

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = CyberBackground,
                            containerColor = CyberSurfaceVariant,
                            labelColor = CyberTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else CyberBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game List
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = CyberTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No games found",
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to add a custom game or tap Scan to discover installed games on this phone.",
                            color = CyberTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.scanInstalledGames() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Installed Games")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        GameCardItem(
                            game = game,
                            onToggleFavorite = { viewModel.toggleFavorite(game.id, game.isFavorite) },
                            onDelete = { viewModel.deleteGame(game) },
                            onHostNow = {
                                viewModel.selectGameForHosting(game)
                                onHostGame(game)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomGameDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, pkg, category, fps, res, layout ->
                viewModel.addCustomGame(
                    title = title,
                    packageName = pkg,
                    category = category,
                    accentColor = "#06B6D4",
                    targetFps = fps,
                    resolution = res,
                    controllerLayout = layout
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun GameCardItem(
    game: GameEntity,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onHostNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_item_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Icon Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3306B6D4))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = game.title,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (game.isFavorite) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = NeonRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = game.packageName,
                        color = CyberTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (game.isFavorite) NeonRed else CyberTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeText(text = game.category, color = NeonViolet)
                BadgeText(text = "${game.targetResolution} / ${game.targetFps}fps", color = NeonCyan)
                BadgeText(text = "${game.preferredControllerLayout} Pad", color = NeonGreen)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CyberTextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Host Now
            Button(
                onClick = onHostNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("host_game_${game.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CyberBackground,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HOST THIS GAME",
                    color = CyberBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
fun BadgeText(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.8.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AddCustomGameDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, pkg: String, category: String, fps: Int, res: String, layout: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Action") }
    var targetFps by remember { mutableIntStateOf(60) }
    var resolution by remember { mutableStateOf("1080p") }
    var controllerLayout by remember { mutableStateOf("Standard") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "PUT NEW GAME IN POCKETLINK",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = CyberTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Game Title (e.g. Minecraft)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. com.mojang.minecraftpe)") },
                    placeholder = { Text("com.example.game") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = controllerLayout,
                        onValueChange = { controllerLayout = it },
                        label = { Text("Controls (FPS/RPG/Pad)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target FPS:", color = CyberTextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = targetFps == 30,
                            onClick = { targetFps = 30 },
                            label = { Text("30") }
                        )
                        FilterChip(
                            selected = targetFps == 60,
                            onClick = { targetFps = 60 },
                            label = { Text("60") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val pkg = if (packageName.isBlank()) "com.pocketlink.custom.${title.lowercase().replace(" ", "")}" else packageName
                        onAdd(title, pkg, category, targetFps, resolution, controllerLayout)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Add Game", color = CyberBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyberTextSecondary)
            }
        },
        containerColor = CyberSurfaceElevated
    )
}
