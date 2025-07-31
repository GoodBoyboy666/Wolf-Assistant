package top.goodboyboy.hutassistant.ui.sanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import top.goodboyboy.hutassistant.R
import java.net.URLEncoder

@Composable
fun ScannerView(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    var isGrated by remember { mutableStateOf(false) }
    val loadAccessToken by viewModel.accessTokenLoadState.collectAsStateWithLifecycle()
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) {
                    isGrated = true
                } else {
                    isGrated = false
                }
            },
        )
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }
    when (loadAccessToken) {
        // 懒得判断了
        is ScannerViewModel.AccessTokenLoadState.Error -> {
            Text("出错了（悲）\n原因：" + (loadAccessToken as ScannerViewModel.AccessTokenLoadState.Error).error)
        }

        ScannerViewModel.AccessTokenLoadState.Idle -> {}
        ScannerViewModel.AccessTokenLoadState.Loading -> {}
        ScannerViewModel.AccessTokenLoadState.Success -> {
            if (isGrated) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CameraPreview { data ->
                        println(data)
                        val encodedUrl = URLEncoder.encode(data, "UTF-8")
                        val headerTokenKeyName = "x-id-token"
                        navController.navigate("browser/$encodedUrl?headerTokenKeyName=$headerTokenKeyName") {
                            popUpTo("scanner") { inclusive = true }
                        }
                    }
                    Box(
                        modifier =
                            Modifier
                                .size(250.dp)
                                .align(Alignment.Center)
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                    )
                }
            } else {
                val context = LocalContext.current
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.allow_camera_permission))
                    Spacer(modifier = Modifier.padding(10.dp))
                    Button(
                        onClick = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        },
                    ) {
                        Text(stringResource(R.string.go_to_setting_page))
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    Text(stringResource(R.string.has_allow_text))
                    Spacer(modifier = Modifier.padding(10.dp))
                    Button(
                        onClick = {
                            launcher.launch(Manifest.permission.CAMERA)
                        },
                    ) {
                        Text(stringResource(R.string.pull_up_the_authorization))
                    }
                }
            }
        }
    }
    BackHandler(enabled = true) {
        isGrated = false
        navController.popBackStack()
    }
}
