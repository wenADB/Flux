package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Optimize : Screen("optimize", "Optimize", Icons.Default.Speed)
    object GameSpace : Screen("game_space", "Game Space", Icons.Default.SportsEsports)
    object Performance : Screen("performance", "Performance", Icons.Default.Analytics)
    object Display : Screen("display", "Display", Icons.Default.Tv)
    object Apps : Screen("apps", "Apps", Icons.Default.Apps)
    object Art : Screen("art", "ART", Icons.Default.DeveloperBoard)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)
}

val BottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Optimize,
    Screen.GameSpace,
    Screen.Performance,
    Screen.History
)
