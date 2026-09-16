package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.shizuku.ShizukuStatus
import com.example.presentation.FluxViewModel
import com.example.presentation.about.AboutScreen
import com.example.presentation.art.ArtScreen
import com.example.presentation.common.ExecutionPhaseDialog
import com.example.presentation.common.FluxPill
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.display.DisplayScreen
import com.example.presentation.gameSpace.GameSpaceScreen
import com.example.presentation.history.HistoryScreen
import com.example.presentation.navigation.BottomNavScreens
import com.example.presentation.navigation.Screen
import com.example.presentation.optimize.OptimizeScreen
import com.example.presentation.packages.BackgroundAppsScreen
import com.example.presentation.performance.PerformanceScreen
import com.example.presentation.settings.SettingsScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FluxTheme
import com.example.ui.theme.RedCritical
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[FluxViewModel::class.java]

        setContent {
            FluxTheme {
                FluxApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxApp(viewModel: FluxViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shizukuState by viewModel.shizukuState.collectAsState()
    val executionPhase by viewModel.executionPhase.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "FLUX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    val shizukuColor = when (shizukuState.status) {
                        ShizukuStatus.PERMISSION_GRANTED -> EmeraldSuccess
                        ShizukuStatus.PERMISSION_MISSING -> AmberWarning
                        else -> RedCritical
                    }
                    IconButton(
                        onClick = { viewModel.requestShizukuPermission() },
                        modifier = Modifier.testTag("shizuku_status_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shizuku Status",
                            tint = shizukuColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.About.route) },
                        modifier = Modifier.testTag("about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                BottomNavScreens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(viewModel = viewModel, navController = navController)
                }
                composable(Screen.Optimize.route) {
                    OptimizeScreen(viewModel = viewModel)
                }
                composable(Screen.GameSpace.route) {
                    GameSpaceScreen(viewModel = viewModel)
                }
                composable(Screen.Performance.route) {
                    PerformanceScreen(viewModel = viewModel)
                }
                composable(Screen.Display.route) {
                    DisplayScreen(viewModel = viewModel)
                }
                composable(Screen.Apps.route) {
                    BackgroundAppsScreen(viewModel = viewModel)
                }
                composable(Screen.Art.route) {
                    ArtScreen(viewModel = viewModel)
                }
                composable(Screen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
                composable(Screen.About.route) {
                    AboutScreen(viewModel = viewModel)
                }
            }

            // Interactive execution status dialog
            ExecutionPhaseDialog(
                phase = executionPhase,
                onDismiss = { viewModel.dismissExecutionResult() }
            )
        }
    }
}
