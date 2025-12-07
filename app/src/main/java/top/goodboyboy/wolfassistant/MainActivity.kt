package top.goodboyboy.wolfassistant

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
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dagger.hilt.android.AndroidEntryPoint
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.ui.appsetting.SettingView
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.util.VersionUpdateChecker
import top.goodboyboy.wolfassistant.ui.components.BottomBar
import top.goodboyboy.wolfassistant.ui.components.TopBar
import top.goodboyboy.wolfassistant.ui.components.TopBarConstants
import top.goodboyboy.wolfassistant.ui.event.TopBarTitleEvent
import top.goodboyboy.wolfassistant.ui.firstpage.FirstPage
import top.goodboyboy.wolfassistant.ui.home.HomeView
import top.goodboyboy.wolfassistant.ui.home.HomeViewModel
import top.goodboyboy.wolfassistant.ui.login.LoginView
import top.goodboyboy.wolfassistant.ui.messagecenter.MessageCenterView
import top.goodboyboy.wolfassistant.ui.messagecenter.MessageCenterViewModel
import top.goodboyboy.wolfassistant.ui.personalcenter.PersonalCenter
import top.goodboyboy.wolfassistant.ui.personalcenter.PersonalCenterViewModel
import top.goodboyboy.wolfassistant.ui.sanner.ScannerView
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleCenterView
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleCenterViewModel
import top.goodboyboy.wolfassistant.ui.servicecenter.ServiceCenterView
import top.goodboyboy.wolfassistant.ui.servicecenter.ServiceCenterViewModel
import top.goodboyboy.wolfassistant.ui.theme.WolfAssistantTheme
import top.goodboyboy.wolfassistant.ui.webview.BrowserView
import top.goodboyboy.wolfassistant.ui.webview.BrowserViewModel
import java.net.URLDecoder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var globalEventBus: GlobalEventBus

    @Inject
    lateinit var versionUpdateChecker: VersionUpdateChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WolfAssistantTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    try {
                        val result = versionUpdateChecker.checkUpdate(BuildConfig.VERSION_NAME)
                        when (result) {
                            is VersionDomainData.Success -> {
                                val snackbarResult =
                                    snackbarHostState.showSnackbar(
                                        message = "发现新版本: ${result.data.versionNameItem.versionNameString}",
                                        actionLabel = "去更新",
                                        duration = androidx.compose.material3.SnackbarDuration.Long,
                                    )
                                if (snackbarResult == SnackbarResult.ActionPerformed) {
                                    navController.navigate("setting")
                                }
                            }
                            is VersionDomainData.NOUpdate -> {
                            }
                            is VersionDomainData.Error -> {
                            }
                        }
                    } catch (_: Exception) {
                    }
                }

                val currentRoute =
                    navController
                        .currentBackStackEntryAsState()
                        .value
                        ?.destination
                        ?.route
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
                        TopBar(navController, globalEventBus)
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
                            LaunchedEffect(Unit) {
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = ScreenRoute.Home.title,
                                    ),
                                )
                            }
                            val viewModel = hiltViewModel<HomeViewModel>(owner)
                            HomeView(
                                innerPadding,
                                navController,
                                snackbarHostState,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.ServiceCenter.route) {
                            LaunchedEffect(Unit) {
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = ScreenRoute.ServiceCenter.title,
                                    ),
                                )
                            }
                            val viewModel = hiltViewModel<ServiceCenterViewModel>(owner)
                            ServiceCenterView(
                                innerPadding,
                                navController,
                                snackbarHostState,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.MessageCenter.route) {
                            LaunchedEffect(Unit) {
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = ScreenRoute.MessageCenter.title,
                                    ),
                                )
                            }
                            val viewModel = hiltViewModel<MessageCenterViewModel>(owner)
                            MessageCenterView(
                                innerPadding,
                                viewModel,
                            )
                        }
                        composable(ScreenRoute.Schedule.route) {
                            val viewModel = hiltViewModel<ScheduleCenterViewModel>(owner)
                            ScheduleCenterView(
                                innerPadding,
                                snackbarHostState,
                                viewModel,
                                globalEventBus,
                            )
                        }
                        composable(ScreenRoute.PersonalCenter.route) {
                            LaunchedEffect(Unit) {
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = ScreenRoute.PersonalCenter.title,
                                    ),
                                )
                            }
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
                                snackbarHostState,
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
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = "",
                                    ),
                                )
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
                                viewModel,
                                globalEventBus,
                            )
                        }
                        composable("scanner") {
                            ScannerView(
                                navController,
                            )
                        }
                        composable("setting") {
                            LaunchedEffect(Unit) {
                                globalEventBus.emit(
                                    TopBarTitleEvent(
                                        targetTag = TopBarConstants.TOP_BAR_TAG,
                                        title = "设置",
                                    ),
                                )
                            }
                            SettingView(
                                navController,
                                innerPadding,
                                snackbarHostState,
                            )
                        }
                        composable("oss") {
                            val libraries by produceLibraries(R.raw.aboutlibraries)
                            LibrariesContainer(
                                libraries = libraries,
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
