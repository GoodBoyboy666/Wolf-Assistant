package top.goodboyboy.hutassistant.ui.appsetting

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hutassistant.BuildConfig
import top.goodboyboy.hutassistant.ui.appsetting.components.UpdateDialog
import top.goodboyboy.hutassistant.ui.components.SettingDivider
import top.goodboyboy.hutassistant.ui.components.SettingItem

@Composable
fun SettingView(
    navController: NavController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val cacheSize = viewModel.cacheSize
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var showLogoutAlert by remember { mutableStateOf(false) }
    var showAboutCard by remember { mutableStateOf(false) }
    val checkUpdateState by viewModel.updateState.collectAsStateWithLifecycle()
    var showUpdateAlert by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.getTotalCacheSize(context)
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
    ) {
        SettingDivider(
            "账户设置",
        )
        SettingItem(
            title = "退出登录",
            subtitle = "退出当前账号",
            icon = Icons.AutoMirrored.Rounded.Logout,
        ) {
            showLogoutAlert = true
        }
        SettingDivider(
            "应用设置",
        )
        SettingItem(
            title = "清除缓存",
            subtitle = "当前缓存占用：${cacheSize.value}",
            icon = Icons.Rounded.Delete,
        ) {
            scope.launch {
                viewModel.cleanAllCache(context)
            }
        }
        SettingDivider(
            "关于",
        )
        SettingItem(
            title = "GitHub",
            subtitle = "代码仓库",
            icon = Icons.Rounded.Code,
        ) {
            val url = "https://github.com/GoodBoyboy666/HUT-Assistant"
            uriHandler.openUri(url)
        }
        var updateSubtitle by remember { mutableStateOf("检查应用版本更新") }
        SettingItem(
            title = "检查更新",
            subtitle = updateSubtitle,
            icon = Icons.Rounded.ArrowUpward,
        ) {
            scope.launch {
                viewModel.getUpdateInfo()
            }
        }
        SettingItem(
            title = "关于我们",
            subtitle = "版本号：${BuildConfig.VERSION_NAME}",
            icon = Icons.Rounded.Info,
        ) {
            showAboutCard = true
        }

        if (showLogoutAlert) {
            AlertDialog(
                icon = {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "登出")
                },
                title = {
                    Text(text = "退出登录")
                },
                text = {
                    Text(text = "您确定要退出当前账号吗？此操作不可撤销。")
                },
                onDismissRequest = {
                    showLogoutAlert = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.logout(context)
                                showLogoutAlert = false
                                withContext(Dispatchers.Main) {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            }
                        },
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLogoutAlert = false
                        },
                    ) {
                        Text("取消")
                    }
                },
            )
        }

        if (showAboutCard) {
            AlertDialog(
                icon = {
                    Icon(Icons.Rounded.Info, contentDescription = "关于")
                },
                title = {
                    Text(text = "HUT Assistant")
                },
                text = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "版本号：${BuildConfig.VERSION_NAME}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp),
                        )
                        Text(
                            text = "编译类型：${BuildConfig.BUILD_TYPE}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp),
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                        Text(
                            text = "made by GoodBoyboy",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                },
                onDismissRequest = {
                    showAboutCard = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAboutCard = false
                        },
                    ) {
                        Text("确定")
                    }
                },
            )
        }

        when (checkUpdateState) {
            is SettingViewModel.CheckUpdateState.Error -> {
                val message = (checkUpdateState as SettingViewModel.CheckUpdateState.Error).error.message
                val stackTrace =
                    (checkUpdateState as SettingViewModel.CheckUpdateState.Error)
                        .error.cause
                        ?.stackTraceToString() ?: ""
                Log.e(
                    null,
                    stackTrace,
                )
                LaunchedEffect(Unit) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message,
                        )
                    }
                    viewModel.changeUpdateState(SettingViewModel.CheckUpdateState.Idle)
                }
            }

            SettingViewModel.CheckUpdateState.Idle -> {
                updateSubtitle = "检查应用版本更新"
            }
            SettingViewModel.CheckUpdateState.Loading -> {
                updateSubtitle = "检查中……"
            }
            is SettingViewModel.CheckUpdateState.Success -> {
                val data = (checkUpdateState as SettingViewModel.CheckUpdateState.Success).data
                if (data == null) {
                    LaunchedEffect(Unit) {
                        scope.launch {
                            snackbarHostState.showSnackbar("当前为最新版本哦~")
                        }
                        viewModel.changeUpdateState(SettingViewModel.CheckUpdateState.Idle)
                    }
                } else {
                    UpdateDialog(
                        versionInfo = data,
                    ) {
                        viewModel.changeUpdateState(SettingViewModel.CheckUpdateState.Idle)
                    }
                }
            }
        }
    }
}
