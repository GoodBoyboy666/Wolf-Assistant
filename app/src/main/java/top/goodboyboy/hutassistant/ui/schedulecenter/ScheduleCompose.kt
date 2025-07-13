package top.goodboyboy.hutassistant.ui.schedulecenter

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
import androidx.compose.runtime.LaunchedEffect
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
import top.goodboyboy.hutassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.math.ceil

@Preview
@Composable
private fun ScheduleComposePreview() {
    val list =
        List(35) { index ->
            ScheduleItem(
                title = "测试课程$index",
                startDate = OffsetDateTime.now(),
                startTime = LocalTime.now(),
                endDate = OffsetDateTime.now(),
                endTime = LocalTime.now(),
                address = "测试地址",
                remark = "测试教师",
                startDateStr = "2025-05-19 08:00:00",
                endDateStr = "2025-05-19 09:40:00",
            )
        }
    ScheduleCompose(list)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCompose(
    schedule: List<ScheduleItem?>,
    modifier: Modifier = Modifier,
) {
    var items by remember { mutableStateOf<List<ScheduleItem?>>(emptyList()) }
    var showScheduleItem by remember { mutableStateOf<ScheduleItem?>(null) }
    var showScheduleDetail by remember { mutableStateOf(false) }
    LaunchedEffect(schedule) {
        items = schedule.reorderByColumn(7)
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
                            showScheduleItem = item
                            showScheduleDetail = true
                        },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (item != null) {
                        Text(
                            item.title,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (showScheduleDetail) {
        ModalBottomSheet(onDismissRequest = { showScheduleDetail = false }) {
            ScheduleDetail(showScheduleItem)
        }
    }
}

fun <T> List<T>.reorderByColumn(columns: Int): List<T> {
    if (columns <= 1) return this

    val numRows = ceil(this.size.toFloat() / columns).toInt()

    val reorderedList = mutableListOf<T>()

    for (row in 0 until numRows) {
        for (col in 0 until columns) {
            val originalIndex = col * numRows + row
            if (originalIndex < this.size) {
                reorderedList.add(this[originalIndex])
            }
        }
    }

    return reorderedList
}
