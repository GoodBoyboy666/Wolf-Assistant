package top.goodboyboy.hutassistant.ui.firstpage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import top.goodboyboy.hutassistant.R
import top.goodboyboy.hutassistant.ScreenRoute

@Composable
fun FirstPage(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: FirstPageViewModel = hiltViewModel(),
) {
    // 此页面用于应用判断是否登录
    val accessToken by viewModel.hasAccessToken.collectAsStateWithLifecycle()
    val loadState by viewModel.loadState
    when (loadState) {
        is FirstPageViewModel.LoadState.Failed -> {}
        FirstPageViewModel.LoadState.Idle -> {
        }

        FirstPageViewModel.LoadState.Loading -> {
            LoadPage(innerPadding)
        }

        FirstPageViewModel.LoadState.Success -> {
            if (accessToken) {
                navController.navigate(ScreenRoute.Home.route) {
                    popUpTo(0)
                }
            } else {
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
