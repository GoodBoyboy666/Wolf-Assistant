package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

@Preview
@Composable
fun LabScheduleComposePreview() {
    val list =
        List(42) { index ->
            LabScheduleItem(
                courseName = "测试课程$index",
                courseCode = "测试Code$index",
                className = "测试教室$index",
                location = "测试地点$index",
                section = "测试节次$index",
            )
        }
    LabScheduleCompose(
        schedule = list,
        modifier = Modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScheduleCompose(
    schedule: List<LabScheduleItem?>,
    modifier: Modifier,
) {
    var showLabScheduleItem by remember { mutableStateOf<LabScheduleItem?>(null) }
    var showLabScheduleDetail by remember { mutableStateOf(false) }
    val items =
        schedule.ifEmpty {
            List(42) { null }
        }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier,
    ) {
        items(items) { item ->
            Card(
                modifier =
                    Modifier
                        .padding(5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxSize()
                        .height(100.dp)
                        .clickable {
                            showLabScheduleItem = item
                            showLabScheduleDetail = true
                        },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (item != null) {
                        Text(
                            item.courseName,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (showLabScheduleDetail) {
        ModalBottomSheet(onDismissRequest = { showLabScheduleDetail = false }) {
            LabScheduleDetail(showLabScheduleItem)
        }
    }
}
