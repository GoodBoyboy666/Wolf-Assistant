package top.goodboyboy.hutassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dagger.hilt.android.AndroidEntryPoint
import top.goodboyboy.hutassistant.ui.appsetting.SettingView
import top.goodboyboy.hutassistant.ui.components.BottomBar
import top.goodboyboy.hutassistant.ui.components.TopBar
import top.goodboyboy.hutassistant.ui.firstpage.FirstPage
import top.goodboyboy.hutassistant.ui.home.HomeView
import top.goodboyboy.hutassistant.ui.home.HomeViewModel
import top.goodboyboy.hutassistant.ui.login.LoginView
import top.goodboyboy.hutassistant.ui.messagecenter.MessageCenterView
import top.goodboyboy.hutassistant.ui.messagecenter.MessageCenterViewModel
import top.goodboyboy.hutassistant.ui.personalcenter.PersonalCenter
import top.goodboyboy.hutassistant.ui.personalcenter.PersonalCenterViewModel
import top.goodboyboy.hutassistant.ui.sanner.ScannerView
import top.goodboyboy.hutassistant.ui.schedulecenter.ScheduleCenterView
import top.goodboyboy.hutassistant.ui.schedulecenter.ScheduleCenterViewModel
import top.goodboyboy.hutassistant.ui.servicecenter.ServiceCenterView
import top.goodboyboy.hutassistant.ui.servicecenter.ServiceCenterViewModel
import top.goodboyboy.hutassistant.ui.theme.HUTAssistantTheme
import top.goodboyboy.hutassistant.ui.webview.BrowserView
import top.goodboyboy.hutassistant.ui.webview.BrowserViewModel
import java.net.URLDecoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HUTAssistantTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val currentRoute =
                    navController
                        .currentBackStackEntryAsState()
                        .value
                        ?.destination
                        ?.route
                var title by remember { mutableStateOf("") }
                var showMenu by remember { mutableStateOf(false) }
                var rollBackToCurrentDate by remember { mutableStateOf(false) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        // 仅在Route中显示底部导航栏
                        if (currentRoute in ScreenRoute.items.map { it.route }) {
                            BottomBar(navController)
                        }
                    },
                    topBar = {
                        if (currentRoute != null &&
                            currentRoute.startsWith(
                                "browser",
                            )
                        ) {
                            TopBar(title, navController, onMenuClick = {
                                showMenu = true
                            })
                        } else if (currentRoute == ScreenRoute.Schedule.route) {
                            TopBar(title, navController, onRollBackToCurrentDate = {
                                rollBackToCurrentDate = true
                            })
                        } else if (currentRoute in ScreenRoute.items.map { it.route }) {
                            TopBar(title, navController)
                        } else if (currentRoute in listOf("setting")) {
                            TopBar(title, navController)
                        }
                    },
                ) { innerPadding ->
                    val owner =
                        checkNotNull(LocalViewModelStoreOwner.current) {
                            "No ViewModelStoreOwner provided"
                        }
                    NavHost(
                        navController = navController,
                        startDestination = "first_page",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300),
                            )
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300),
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300),
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300),
                            )
                        },
                    ) {
                        composable("login") {
                            LoginView(
                                innerPadding,
                                navController,
                                snackbarHostState,
                            )
                        }
                        composable(ScreenRoute.Home.route) {
//                            val context = LocalContext.current
//                            BackHandler(enabled = true) {
//                                (context as? Activity)?.finish()
//                            }
                            title = ScreenRoute.Home.title
                            val viewModel = hiltViewModel<HomeViewModel>(owner)
                            HomeView(
                                innerPadding,
                                navController,
                                snackbarHostState,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.ServiceCenter.route) { backStackEntry ->
                            title = ScreenRoute.ServiceCenter.title
                            val viewModel = hiltViewModel<ServiceCenterViewModel>(owner)
                            ServiceCenterView(
                                innerPadding,
                                navController,
                                snackbarHostState,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.MessageCenter.route) {
                            title = ScreenRoute.MessageCenter.title
                            val viewModel = hiltViewModel<MessageCenterViewModel>(owner)
                            MessageCenterView(
                                innerPadding,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.Schedule.route) { backStackEntry ->
                            val viewModel = hiltViewModel<ScheduleCenterViewModel>(owner)
                            ScheduleCenterView(
                                innerPadding,
                                snackbarHostState,
                                viewModel,
                                { week ->
                                    title = "${week.year}年${week.monthValue}月"
                                },
                                rollBackToCurrentDate,
                                {
                                    rollBackToCurrentDate = false
                                },
                            )
                        }
                        composable(ScreenRoute.PersonalCenter.route) { backStackEntry ->
                            title = ScreenRoute.PersonalCenter.title
                            val viewModel = hiltViewModel<PersonalCenterViewModel>(owner)
                            PersonalCenter(
                                innerPadding,
                                navController,
                                snackbarHostState,
                                viewModel,
                            )
                        }
                        composable("first_page") {
                            FirstPage(
                                innerPadding,
                                navController,
                            )
                        }
                        composable(
                            "browser/{url}?headerTokenKeyName={headerTokenKeyName}&urlTokenKeyName={urlTokenKeyName}",
                            arguments =
                                listOf(
                                    navArgument("url") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    },
                                    navArgument("headerTokenKeyName") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    },
                                    navArgument("urlTokenKeyName") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    },
                                ),
                        ) { backStackEntry ->
                            val viewModel = hiltViewModel<BrowserViewModel>(owner)
                            LaunchedEffect(Unit) {
                                title = ""
                            }
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            val originalUrl =
                                URLDecoder.decode(
                                    url,
                                    "UTF-8",
                                )
                            val headerTokenKeyName =
                                backStackEntry.arguments?.getString("headerTokenKeyName") ?: ""
                            val urlTokenKeyName =
                                backStackEntry.arguments?.getString("urlTokenKeyName") ?: ""
                            BrowserView(
                                originalUrl,
                                headerTokenKeyName,
                                urlTokenKeyName,
                                navController,
                                snackbarHostState,
                                innerPadding,
                                showMenu,
                                viewModel,
                                { titleText ->
                                    title = titleText
                                },
                                {
                                    title = ""
                                },
                                {
                                    showMenu = false
                                },
                            )
                        }
                        composable("scanner") {
                            ScannerView(
                                navController,
                            )
                        }
                        composable("setting") {
                            title = "设置"
                            SettingView(
                                navController,
                                innerPadding,
                                snackbarHostState,
                            )
                        }
                        composable("oss") {
                            LibrariesContainer(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}
