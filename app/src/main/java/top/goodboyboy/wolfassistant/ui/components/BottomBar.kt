package top.goodboyboy.wolfassistant.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import top.goodboyboy.wolfassistant.ScreenRoute

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        ScreenRoute.items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            popUpTo(0)
                        }
                    }
                },
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title,
                    )
                },
                label = { Text(screen.title) },
            )
        }
    }
}
