package top.goodboyboy.wolfassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem

@Preview
@Composable
private fun InfoItemPreview() {
    InfoItem(
        PortalInfoItem(
            "测试标题",
            "测试作者",
            "2025.05.10",
            "https://www.bing.com",
        ),
    ) { _ -> }
}

@Composable
fun InfoItem(
    item: PortalInfoItem,
    onClick: (String) -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier =
            Modifier
                .clip(shape)
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        onClick(item.url)
                    },
                ),
    ) {
        Text(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp),
            text = item.title,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.padding(end = 10.dp),
                text = item.author,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = item.createTime,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
