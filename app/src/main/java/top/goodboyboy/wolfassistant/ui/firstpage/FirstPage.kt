package top.goodboyboy.wolfassistant.ui.firstpage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ScreenRoute

@Composable
fun FirstPage(
    innerPadding: PaddingValues,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: FirstPageViewModel = hiltViewModel(),
) {
    // 此页面用于应用判断是否登录
    val accessToken by viewModel.hasAccessToken.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val hasTokenExpired by viewModel.hasTokenExpired.collectAsStateWithLifecycle()
//    LaunchedEffect(Unit) {
//        viewModel.checkLoginStatue()
//    }
    val scope = rememberCoroutineScope()
    when (val state = loadState) {
        is FirstPageViewModel.LoadState.Failed -> {
            LoadPage(innerPadding)
            scope.launch {
                snackbarHostState.showSnackbar(state.message)
            }
        }

        FirstPageViewModel.LoadState.Idle, FirstPageViewModel.LoadState.Loading -> {
            LoadPage(innerPadding)
        }

        FirstPageViewModel.LoadState.Success -> {
            if (accessToken && !hasTokenExpired) {
                navController.navigate(ScreenRoute.Home.route) {
                    popUpTo(0)
                }
            }else if(hasTokenExpired){
                TokenExpiredDialog(
                    {
                        navController.navigate(ScreenRoute.Home.route) {
                            popUpTo(0)
                        }
                    },
                    {
                        scope.launch {
                            viewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }
            else {
                navController.navigate("login") {
                    popUpTo(0)
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadPagePreview() {
    LoadPage(PaddingValues(0.dp))
}

@Composable
fun LoadPage(innerPadding: PaddingValues) {
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.please_wait),
                modifier =
                    Modifier.align(
                        Alignment.Center,
                    ),
            )
            Text(
                "Copyright © 2025 GoodBoyboy. All Rights Reserved.",
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter,
                    ),
            )
        }
    }
}

@Preview
@Composable
fun TokenExpiredDialogPreview() {
    TokenExpiredDialog(
        onDismissRequest = {},
        onConfirmRequest = {},
    )
}

@Composable
fun TokenExpiredDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest:()-> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Update,"登录状态过期")
        },
        title = {
            Text(text = "登录状态已过期")
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "您的登录状态已过期，请重新登录以继续使用应用的全部功能。取消仍可使用部分功能，但可能会受到限制。",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(5.dp),
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text("取消")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmRequest()
                },
            ) {
                Text("确定")
            }
        },
    )
}
