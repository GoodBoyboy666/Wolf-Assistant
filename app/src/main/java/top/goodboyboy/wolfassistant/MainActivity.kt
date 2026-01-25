package top.goodboyboy.wolfassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.appsetting.SettingView
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.repository.UpdateRepository
import top.goodboyboy.wolfassistant.ui.components.BottomBar
import top.goodboyboy.wolfassistant.ui.components.PadNavigationRail
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
import top.goodboyboy.wolfassistant.util.CrashInfoUtil
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLDecoder
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var globalEventBus: GlobalEventBus

    @Inject
    lateinit var updateRepository: UpdateRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }
        setContent {
            WolfAssistantTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val windowSizeClass = calculateWindowSizeClass(this)
                val isMobile = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

                LaunchedEffect(Unit) {
                    try {
                        val result =
                            updateRepository.checkUpdate(
                                BuildConfig.VERSION_NAME,
                                settingsRepository.enablePreRelease.first(),
                            )
                        when (result) {
                            is VersionDomainData.Success -> {
                                val snackbarResult =
                                    snackbarHostState.showSnackbar(
                                        message = "发现新版本: ${result.data.version}",
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        if (isMobile) {
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
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (!isMobile) {
                            PadNavigationRail(
                                navController,
                                Modifier.padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = innerPadding.calculateBottomPadding(),
                                ),
                            )
                        }
                        val inTransition =
                            if (isMobile) {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300),
                                )
                            } else {
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(300),
                                )
                            }
                        val outTransition =
                            if (isMobile) {
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300),
                                )
                            } else {
                                slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(300),
                                )
                            }
                        NavHost(
                            modifier = Modifier.weight(1f),
                            navController = navController,
                            startDestination = "first_page",
                            enterTransition = {
                                inTransition
                            },
                            exitTransition = {
                                outTransition
                            },
                            popEnterTransition = {
                                inTransition
                            },
                            popExitTransition = {
                                outTransition
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
                                val layoutDirection = LocalLayoutDirection.current
                                LibrariesContainer(
                                    libraries = libraries,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(
                                                start = innerPadding.calculateStartPadding(layoutDirection),
                                                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                                                end = innerPadding.calculateEndPadding(layoutDirection),
                                                bottom =
                                                    WindowInsets.navigationBars
                                                        .asPaddingValues()
                                                        .calculateBottomPadding(),
                                            ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleUncaughtException(
        thread: Thread,
        throwable: Throwable,
    ) {
        Log.e("AppCrash", "检测到未捕获异常，线程: ${thread.name}", throwable)
        Toast.makeText(this, "程序发生崩溃，正在收集日志...", Toast.LENGTH_LONG).show()
        val deviceInfo = CrashInfoUtil.getDeviceAndAppInfo(this)
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTraceString = sw.toString()
        try {
            val fileName = "crash_log_${System.currentTimeMillis()}.txt"
            val crashFile = File(cacheDir, fileName)
            val fullCrashReport =
                """
        |========================================
        |              DEVICE INFO
        |========================================
        |$deviceInfo
        |
        |========================================
        |              STACK TRACE
        |========================================
        |$stackTraceString
        |
        |========================================
        |              END REPORT
        |========================================
                """.trimMargin()
            crashFile.writeText(fullCrashReport)
            val intent =
                Intent(this, UncaughtExceptionActivity::class.java).apply {
                    putExtra("crash_file_path", crashFile.absolutePath)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
}
