package com.tether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.data.health.HealthConnectManager
import com.tether.data.repository.UserRepository
import com.tether.ui.screens.auth.AuthScreen
import com.tether.ui.screens.diary.DiaryScreen
import com.tether.ui.screens.profile.ProfileScreen
import com.tether.ui.screens.routes.RoutesScreen
import com.tether.ui.screens.stats.StatsScreen
import com.tether.ui.screens.workouts.WorkoutsScreen
import com.tether.ui.theme.TetherTheme

enum class TetherDestination(val label: String, val icon: ImageVector) {
    DIARY("Дневник", Icons.Default.Restaurant),
    WORKOUTS("Тренировки", Icons.Default.FitnessCenter),
    ROUTES("Маршруты", Icons.Default.Map),
    STATS("Аналитика", Icons.Default.BarChart),
    PROFILE("Профиль", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        healthConnectManager = HealthConnectManager(applicationContext)

        setContent {
            TetherTheme {
                val isLoggedIn by UserRepository.isLoggedIn.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        AuthScreen(
                            onAuthSuccess = {
                                // Profile is active
                            }
                        )
                    } else {
                        TetherMainNavigationApp(healthConnectManager = healthConnectManager)
                    }
                }
            }
        }
    }
}

@Composable
fun TetherMainNavigationApp(
    healthConnectManager: HealthConnectManager
) {
    var currentDestination by remember { mutableStateOf(TetherDestination.DIARY) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                TetherDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { destination ->
            when (destination) {
                TetherDestination.DIARY -> DiaryScreen(healthConnectManager = healthConnectManager)
                TetherDestination.WORKOUTS -> WorkoutsScreen()
                TetherDestination.ROUTES -> RoutesScreen()
                TetherDestination.STATS -> StatsScreen(healthConnectManager = healthConnectManager)
                TetherDestination.PROFILE -> ProfileScreen(healthConnectManager = healthConnectManager)
            }
        }
    }
}