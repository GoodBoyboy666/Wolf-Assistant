package top.goodboyboy.hutassistant.ui.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.goodboyboy.hutassistant.ui.components.LoadingCompose

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
    val accessToken = viewModel.accessTokenStateFlow.collectAsState()
    var showWebView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showWebView = true
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (accessToken.value != null && showWebView) {
            WebViewCompose(
                url = url,
                accessToken = accessToken.value!!,
                headerTokenKeyName = headerTokenKeyName,
                urlTokenKeyName = urlTokenKeyName,
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
                        snackbarHostState.showSnackbar("无法调起应用，请检查是否安装了相关应用！")
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
                        showWebView = false
                        delay(100)
                        navController.popBackStack()
                    }
                },
            )
        }
        if (accessToken.value == null && showWebView == false) {
            LoadingCompose("正在拉取令牌，请稍等~")
        }
        if (showMenu) {
            MenuCompose(
                // 为确保安全仅传递URL
                url,
            ) {
                onMenuDismissRequest()
            }
        }
    }
}
