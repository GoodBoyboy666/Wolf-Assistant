package top.goodboyboy.wolfassistant.ui.webview

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var locationPermissionCallback by remember { mutableStateOf<GeolocationPermissions.Callback?>(null) }
    var locationPermissionOrigin by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                locationPermissionCallback?.invoke(locationPermissionOrigin, true, false)
            } else {
                locationPermissionCallback?.invoke(locationPermissionOrigin, false, false)
            }
            locationPermissionCallback = null
            locationPermissionOrigin = null
        }
    val scope = rememberCoroutineScope()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val refreshEvent by viewModel.refreshEvent.collectAsStateWithLifecycle()
    // Precompute the localized message at composition time so we don't call a @Composable from a coroutine
    val cantPullUpMessage = stringResource(R.string.cant_pull_up_app)
    val layoutDirection = LocalLayoutDirection.current

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
                LaunchedEffect(state) {
                    scope.launch {
                        snackbarHostState.showSnackbar(state.message)
                    }
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
                        val hasFineLocation =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) == PackageManager.PERMISSION_GRANTED
                        val hasCoarseLocation =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ) == PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation || hasCoarseLocation) {
                            callback.invoke(origin, true, false)
                        } else {
                            locationPermissionCallback = callback
                            locationPermissionOrigin = origin
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    { _, _ ->
//                scope.launch {
//                    snackbarHostState.showSnackbar(error?.description.toString())
//                }
                    },
                    {
                        scope.launch {
                            snackbarHostState.showSnackbar(cantPullUpMessage)
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
