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
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose

@Composable
fun BrowserView(
    url: String,
    headerTokenKeyName: String,
    urlTokenKeyName: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues,
    showMenu: Boolean,
    viewModel: BrowserViewModel,
    onTitleReceived: (String) -> Unit,
    onBrowserDispose: () -> Unit,
    onMenuDismissRequest: () -> Unit,
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val refreshEvent by viewModel.refreshEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                        onTitleReceived(title)
                    },
                    {
                        onBrowserDispose()
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
                            onMenuDismissRequest()
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
