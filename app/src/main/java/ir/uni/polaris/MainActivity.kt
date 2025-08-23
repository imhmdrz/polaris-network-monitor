package ir.uni.polaris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import ir.uni.polaris.ui.home.HistoryScreen
import ir.uni.polaris.ui.home.PolarisScreen
import ir.uni.polaris.ui.home.TestsScreen
import ir.uni.polaris.ui.login.AuthViewModel
import ir.uni.polaris.ui.login.LoginScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val session: AuthViewModel = hiltViewModel()
                val token by session.token.collectAsState()

                if (token.isNullOrBlank()) {
                    LoginScreen(onLoggedIn = {

                    })
                } else {
                    val nav = rememberNavController()

                    data class Tab(val route: String, val label: String, val iconRes: Int)

                    val tabs = listOf(
                        Tab(NavRoutes.Polaris, "Polaris", android.R.drawable.ic_menu_mylocation),
                        Tab(
                            NavRoutes.History,
                            "History",
                            android.R.drawable.ic_menu_recent_history
                        ),
                        Tab(NavRoutes.Tests, "Tests", android.R.drawable.ic_menu_manage)
                    )
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                val current =
                                    nav.currentBackStackEntryAsState().value?.destination?.route
                                tabs.forEach { t ->
                                    NavigationBarItem(
                                        selected = current == t.route,
                                        onClick = {
                                            if (current != t.route) {
                                                nav.navigate(t.route) {
                                                    popUpTo(nav.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true; restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                painterResource(t.iconRes),
                                                contentDescription = t.label
                                            )
                                        },
                                        label = { Text(t.label) }
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        NavHost(navController = nav, startDestination = NavRoutes.Polaris) {
                            composable(NavRoutes.Polaris) { PolarisScreen(paddingValues = padding) }
                            composable(NavRoutes.History) { HistoryScreen(padding) }
                            composable(NavRoutes.Tests) { TestsScreen(padding) }
                        }
                    }
                }
            }
        }
    }
}
