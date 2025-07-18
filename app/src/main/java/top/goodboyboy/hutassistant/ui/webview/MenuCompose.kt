package top.goodboyboy.hutassistant.ui.webview

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCompose(
    url: String,
    onDismissRequest: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = { onDismissRequest() }) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val sendIntent =
                                Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, url)
                                    type = "text/plain"
                                }
                            val shareIntent = Intent.createChooser(sendIntent, "通过以下方式分享")
                            context.startActivity(shareIntent)
                            onDismissRequest()
                        }.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Share, "分享")
                Text("分享")
            }
            Column(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onRefreshClick()
                            onDismissRequest()
                        }.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Refresh, "刷新")
                Text("刷新")
            }
        }
    }
}
