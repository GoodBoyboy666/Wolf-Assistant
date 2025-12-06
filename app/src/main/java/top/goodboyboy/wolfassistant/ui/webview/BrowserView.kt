package top.goodboyboy.wolfassistant.ui.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.components.TopBarConstants
import top.goodboyboy.wolfassistant.ui.event.BrowserMenuClickEvent
import top.goodboyboy.wolfassistant.ui.event.TopBarTitleEvent

@Composable
fun BrowserView(
    url: String,
    headerTokenKeyName: String,
    urlTokenKeyName: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues,
    viewModel: BrowserViewModel,
    globalEventBus: GlobalEventBus,
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val refreshEvent by viewModel.refreshEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        globalEventBus.subscribeToTarget<BrowserMenuClickEvent>("BrowserView").collect {
            showMenu = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                globalEventBus.emit(
                    TopBarTitleEvent(
                        targetTag = TopBarConstants.TOP_BAR_TAG,
                        title = "",
                    ),
                )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (val state = loadState) {
            is BrowserViewModel.LoadState.Failed -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }

            BrowserViewModel.LoadState.Idle, BrowserViewModel.LoadState.Loading -> {
                LoadingCompose()
            }

            is BrowserViewModel.LoadState.Success -> {
                if (loading) {
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                WebViewCompose(
                    url = url,
                    accessToken = state.accessToken,
                    headerTokenKeyName = headerTokenKeyName,
                    urlTokenKeyName = urlTokenKeyName,
                    refreshEvent = refreshEvent,
                    {
                        currentProgress = 0f
                        loading = true
                    },
                    {
                        loading = false
                    },
                    { progress ->
                        scope.launch {
                            currentProgress = progress / 100f
                        }
                    },
                    { origin, callback ->
                        callback.invoke(origin, true, false)
                    },
                    { request, error ->
//                scope.launch {
//                    snackbarHostState.showSnackbar(error?.description.toString())
//                }
                    },
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.cant_pull_up_app))
                        }
                    },
                    { title ->
                        scope.launch {
                            globalEventBus.emit(
                                TopBarTitleEvent(
                                    targetTag = TopBarConstants.TOP_BAR_TAG,
                                    title = title,
                                ),
                            )
                        }
                    },
                    {
                        scope.launch {
                            globalEventBus.emit(
                                TopBarTitleEvent(
                                    targetTag = TopBarConstants.TOP_BAR_TAG,
                                    title = "",
                                ),
                            )
                        }
                    },
                    {
                        scope.launch {
                            loading = false
//                        delay(100)
                            navController.popBackStack()
                        }
                    },
                )
                if (showMenu) {
                    MenuCompose(
                        // 为确保安全仅传递URL
                        url,
                        {
                            showMenu = false
                        },
                        {
                            viewModel.onRefresh()
                        },
                    )
                }
            }
        }
    }
}
