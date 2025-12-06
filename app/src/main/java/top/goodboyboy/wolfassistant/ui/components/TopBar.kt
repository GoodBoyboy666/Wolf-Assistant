package top.goodboyboy.wolfassistant.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ScreenRoute
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleCenterViewModel
import top.goodboyboy.wolfassistant.ui.schedulecenter.event.RollBackToCurrentDateEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "",
    navController: NavController,
    globalEventBus: GlobalEventBus? = null,
    onMenuClick: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    var showNavigationIcon by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            AnimatedContent(
                // title优先级最高，然后是Route的名称
                targetState =
                    if (title != "") {
                        title
                    } else {
                        ScreenRoute.items.firstOrNull { it.route == currentRoute }?.title
                            ?: ""
                    },
                label = "CoverAnimation",
                transitionSpec = {
                    val enter =
                        slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { fullHeight -> -fullHeight },
                        ) + fadeIn()
                    val exit = ExitTransition.None
                    enter togetherWith exit
                },
            ) { text ->
                Text(
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            // 设置页面和浏览器页面均使用回退按钮
            if (currentRoute in listOf("setting") ||
                currentRoute != null &&
                currentRoute.startsWith(
                    "browser",
                )
            ) {
                showNavigationIcon = true
            } else {
                showNavigationIcon = false
            }
            AnimatedVisibility(
                visible = showNavigationIcon,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            }
        },
        actions = {
            // 浏览器页面和课表页面展示action按钮
            if (currentRoute != null &&
                currentRoute.startsWith(
                    "browser",
                )
            ) {
                showActions = true
            } else if (currentRoute != null && currentRoute == ScreenRoute.Schedule.route) {
                showActions = true
            } else {
                showActions = false
            }

            AnimatedVisibility(
                visible = showActions,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
            ) {
                // 课表页面为返回当前周按钮
                if (currentRoute != null && currentRoute == ScreenRoute.Schedule.route) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                globalEventBus?.emit(
                                    RollBackToCurrentDateEvent(
                                        targetTag = ScheduleCenterViewModel.SCHEDULE_CENTER_TAG,
                                    ),
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.History, stringResource(R.string.go_back_to_the_current_week))
                    }
                } else if (currentRoute != null &&
                    // 浏览器页面为浏览器菜单
                    currentRoute.startsWith(
                        "browser",
                    )
                ) {
                    IconButton(onClick = {
                        onMenuClick()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.browser_menu),
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
