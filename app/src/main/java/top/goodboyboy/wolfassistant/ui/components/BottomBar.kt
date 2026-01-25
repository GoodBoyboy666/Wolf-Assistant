package top.goodboyboy.wolfassistant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
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
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current
    // 仅在Route中显示底部导航栏
    AnimatedVisibility(
        visible = currentRoute in ScreenRoute.items.map { it.route },
        enter = slideInVertically(initialOffsetY = { it }) + expandVertically(expandFrom = Alignment.Top),
        exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
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
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    label = { Text(screen.title) },
                )
            }
        }
    }
}
