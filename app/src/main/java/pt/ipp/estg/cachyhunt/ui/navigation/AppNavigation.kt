package pt.ipp.estg.cachyhunt.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import pt.ipp.estg.cachyhunt.ui.screens.auth.RegisterScreen
import pt.ipp.estg.cachyhunt.ui.screens.auth.LoginScreen
import pt.ipp.estg.cachyhunt.ui.screens.map.MapScreen
import pt.ipp.estg.cachyhunt.ui.screens.menu.DetailsGeocacheScreen
import pt.ipp.estg.cachyhunt.ui.screens.menu.MainScreen
import pt.ipp.estg.cachyhunt.ui.screens.aboutus.AboutUsScreen
import pt.ipp.estg.cachyhunt.ui.screens.leaderboard.LeaderboardScreen
import pt.ipp.estg.cachyhunt.ui.screens.menu.CreateGeocacheScreen
import pt.ipp.estg.cachyhunt.ui.screens.profile.EditProfilePage
import pt.ipp.estg.cachyhunt.ui.screens.profile.ProfilePage
import pt.ipp.estg.cachyhunt.ui.theme.MediumGreen
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.ui.theme.White
import pt.ipp.estg.cachyhunt.R
import androidx.compose.ui.res.stringResource
import pt.ipp.estg.cachyhunt.ui.screens.profile.CachesCounter
import pt.ipp.estg.cachyhunt.ui.screens.profile.CapturedGeocachesScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var userEmail: String? = null

    Scaffold(
        bottomBar = {
            if (currentRoute != "login" && currentRoute != "register") {
                NavigationBar(
                    modifier = Modifier.height(70.dp),
                    containerColor = NatureGreen
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home/{userEmail}",
                        onClick = {
                            Log.d("CurrentRoute", userEmail.toString())
                            navController.navigate("home/$userEmail")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            unselectedIconColor = White,
                            selectedTextColor = White,
                            unselectedTextColor = White,
                            indicatorColor = MediumGreen
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text(stringResource(R.string.map))  },
                        selected = currentRoute == "map/{userEmail}",
                        onClick = { navController.navigate("map/$userEmail") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            unselectedIconColor = White,
                            selectedTextColor = White,
                            unselectedTextColor = White,
                            indicatorColor = MediumGreen
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                        label = { Text(stringResource(R.string.profile)) },
                        selected = currentRoute == "profile/{userEmail}",
                        onClick = {
                            Log.d("CurrentRoute", userEmail.toString())
                            navController.navigate("profile/$userEmail") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = White,
                            unselectedIconColor = White,
                            selectedTextColor = White,
                            unselectedTextColor = White,
                            indicatorColor = MediumGreen
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "login", modifier = Modifier.padding(innerPadding)) {
            composable("home/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                if (userEmail != null) {
                    MainScreen(
                        userEmail = userEmail!!,
                        onClick = { geocacheId ->
                            navController.navigate("detailsGeocache/$geocacheId")
                        },
                        onAddClick = { userEmail ->
                            navController.navigate("createPage/$userEmail")
                        },
                        onAboutUsClick = {
                            navController.navigate("aboutUsPage")
                        },
                        onLeaderboardClick = {
                            navController.navigate("leaderboardPage")
                        }
                    )
                }
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { userEmail ->
                        navController.navigate("home/$userEmail") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { userEmail ->
                        navController.navigate("home/$userEmail") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login")
                    }
                )
            }
            composable("profile/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                if (userEmail != null) {
                    ProfilePage(
                        userEmail = userEmail!!,
                        visitGeocaches = {
                            navController.navigate("geocachesCapturedByUser/$userEmail")
                        },
                        onEditClick = {
                            navController.navigate("editProfilePage/$userEmail")
                        },
                        onLogoutClick = {
                            navController.navigate("login")
                        }
                    )
                }
            }

            composable("geocachesCapturedByUser/{userEmail}"){ backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                if (userEmail != null) {
                    CapturedGeocachesScreen(
                        userEmail = userEmail!!,
                        onClick = { geocacheId ->
                            navController.navigate("detailsGeocache/$geocacheId")
                        }
                    )
                }
            }

            composable("detailsGeocache/{geocacheId}") { backStackEntry ->
                val geocacheId = backStackEntry.arguments?.getString("geocacheId")?.toIntOrNull()
                geocacheId?.let { id ->
                    DetailsGeocacheScreen(geocacheId = id)
                }
            }
            composable("map/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                if (userEmail != null) {
                    MapScreen(userEmail = userEmail!!)
                }
            }
            composable("createPage/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                userEmail?.let { email ->
                    CreateGeocacheScreen(
                        userEmail = email,
                        onSuccess = {
                            navController.navigate("home/$email")
                        }
                    )
                }
            }
            composable("aboutUsPage"){
                AboutUsScreen()
            }
            composable("leaderboardPage"){
                LeaderboardScreen()
            }
            composable("editProfilePage/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                EditProfilePage(
                    userEmail = userEmail,
                    onBack = {
                        navController.navigate("home/{userEmail}")
                    }
                )
            }

            composable("capturedGeocaches/{userEmail}") { backStackEntry ->
                userEmail = backStackEntry.arguments?.getString("userEmail")
                CapturedGeocachesScreen(
                    userEmail = userEmail!!,
                    onClick = { geocacheId ->
                        navController.navigate("detailsGeocache/$geocacheId")
                    }
                )
            }
        }
    }
}