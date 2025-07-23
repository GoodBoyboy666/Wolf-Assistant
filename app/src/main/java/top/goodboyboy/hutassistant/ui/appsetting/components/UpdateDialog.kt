package top.goodboyboy.hutassistant.ui.appsetting.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionInfo
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionNameItem

@Preview
@Composable
fun UpdateDialogPreview() {
    UpdateDialog(
        VersionInfo(
            versionNameItem =
                VersionNameItem(
                    majorVersionNumber = 1,
                    secondaryVersionNumber = 0,
                    revisionVersionNumber = 0,
                    others = "",
                    versionNameString = "v1.0.0",
                ),
            htmlUrl = "",
            isPrerelease = false,
            body = "",
        ),
    ) { }
}

@Composable
fun UpdateDialog(
    versionInfo: VersionInfo,
    onDismissRequest: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Update, contentDescription = "新版本")
        },
        title = {
            Text(text = "发现新版本")
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
                    text = "版本号：${versionInfo.versionNameItem.versionNameString}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(5.dp),
                )
                Text(
                    text = "版本类型：" + if (versionInfo.isPrerelease) "预览版" else "正式版",
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
                    onDismissRequest()
                    uriHandler.openUri(versionInfo.htmlUrl)
                },
            ) {
                Text("确定")
            }
        },
    )
}
