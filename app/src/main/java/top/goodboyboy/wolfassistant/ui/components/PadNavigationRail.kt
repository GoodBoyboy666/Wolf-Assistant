package top.goodboyboy.wolfassistant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import top.goodboyboy.wolfassistant.ScreenRoute

@Composable
fun PadNavigationRail(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current
    AnimatedVisibility(
        visible = currentRoute in ScreenRoute.items.map { it.route },
        enter = slideInHorizontally(initialOffsetX = { -it }) + expandHorizontally(expandFrom = Alignment.Start),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkHorizontally(shrinkTowards = Alignment.Start),
    ) {
        NavigationRail {
            ScreenRoute.items.forEach { screen ->
                NavigationRailItem(
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
}
