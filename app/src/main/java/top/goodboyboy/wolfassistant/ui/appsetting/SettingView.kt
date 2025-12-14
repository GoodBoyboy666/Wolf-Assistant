package top.goodboyboy.wolfassistant.ui.appsetting

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.BuildConfig
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.appsetting.components.UpdateDialog
import top.goodboyboy.wolfassistant.ui.components.SettingDivider
import top.goodboyboy.wolfassistant.ui.components.SettingItem
import top.goodboyboy.wolfassistant.ui.components.SwitchSettingItem

@Composable
fun SettingView(
    navController: NavController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val cacheSize by viewModel.cacheSize.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var showLogoutAlert by remember { mutableStateOf(false) }
    var showAboutCard by remember { mutableStateOf(false) }
    val checkUpdateState by viewModel.updateState.collectAsStateWithLifecycle()
    val disableSSLCertVerification by viewModel.disableSSLCertVerification.collectAsStateWithLifecycle(
        initialValue = false,
    )
    val onlyIPv4 by viewModel.onlyIPv4.collectAsStateWithLifecycle(
        initialValue = false,
    )
    val scrollState = rememberScrollState()
    val latestVersion = stringResource(R.string.latest_version)
    LaunchedEffect(Unit) {
        viewModel.getTotalCacheSize(context)
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
    ) {
        SettingDivider(
            stringResource(R.string.account_settings),
        )
        SettingItem(
            title = stringResource(R.string.log_out),
            subtitle = stringResource(R.string.log_out_of_your_current_account),
            icon = Icons.AutoMirrored.Rounded.Logout,
        ) {
            showLogoutAlert = true
        }
        SettingDivider(
            stringResource(R.string.app_settings),
        )
        SettingItem(
            title = stringResource(R.string.clear_cache),
            subtitle = stringResource(R.string.current_cache_occupancy, cacheSize),
            icon = Icons.Rounded.Delete,
        ) {
            scope.launch {
                viewModel.cleanAllCache(context)
            }
        }
        SwitchSettingItem(
            title = "禁用SSL证书检查",
            subtitle = "开启将不检查SSL证书合法性，修改将于下次启动生效",
            icon = Icons.Rounded.Security,
            checked = disableSSLCertVerification,
        ) {
            scope.launch {
                viewModel.setSSLCertVerification(!disableSSLCertVerification)
            }
        }
        SwitchSettingItem(
            title = "仅IPv4",
            subtitle = "开启将仅使用IPv4访问，修改将于下次启动生效",
            icon = Icons.Rounded.Dns,
            checked = onlyIPv4,
        ) {
            scope.launch {
                viewModel.setOnlyIPv4(!onlyIPv4)
            }
        }
        SettingDivider(
            stringResource(R.string.about),
        )
        SettingItem(
            title = "GitHub",
            subtitle = stringResource(R.string.code_repositories),
            icon = Icons.Rounded.Code,
        ) {
            val url = "https://github.com/GoodBoyboy666/HUT-Assistant"
            uriHandler.openUri(url)
        }
        val updateSubtitleStringResource = stringResource(R.string.check_for_app_version_updates)
        var updateSubtitle by remember { mutableStateOf(updateSubtitleStringResource) }
        SettingItem(
            title = stringResource(R.string.check_for_updates),
            subtitle = updateSubtitle,
            icon = Icons.Rounded.ArrowUpward,
        ) {
            scope.launch {
                viewModel.getUpdateInfo()
            }
        }
        SettingItem(
            title = stringResource(R.string.about_us),
            subtitle = stringResource(R.string.version_number, BuildConfig.VERSION_NAME),
            icon = Icons.Rounded.Info,
        ) {
            showAboutCard = true
        }

        if (showLogoutAlert) {
            AlertDialog(
                icon = {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = stringResource(R.string.log_out))
                },
                title = {
                    Text(text = stringResource(R.string.log_out))
                },
                text = {
                    Text(text = stringResource(R.string.log_out_warning))
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
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLogoutAlert = false
                        },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (showAboutCard) {
            AlertDialog(
                icon = {
                    Icon(Icons.Rounded.Info, contentDescription = stringResource(R.string.about))
                },
                title = {
                    Text(text = "Wolf Assistant")
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
                            text = stringResource(R.string.version_number, BuildConfig.VERSION_NAME),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(5.dp),
                        )
                        Text(
                            text = stringResource(R.string.release_type) + BuildConfig.BUILD_TYPE,
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
                        Text(stringResource(R.string.confirm))
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
                updateSubtitle = stringResource(R.string.check_for_app_version_updates)
            }

            SettingViewModel.CheckUpdateState.Loading -> {
                updateSubtitle = stringResource(R.string.checking)
            }

            is SettingViewModel.CheckUpdateState.Success -> {
                val data = (checkUpdateState as SettingViewModel.CheckUpdateState.Success).data
                if (data == null) {
                    LaunchedEffect(Unit) {
                        scope.launch {
                            snackbarHostState.showSnackbar(latestVersion)
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
