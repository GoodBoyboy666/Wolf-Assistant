package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalTime
import java.time.OffsetDateTime

@Preview
@Composable
private fun ScheduleDetailPreview() {
    val item =
        ScheduleItem(
            title = "测试课程",
            startDate = OffsetDateTime.now(),
            startTime = LocalTime.now(),
            endDate = OffsetDateTime.now(),
            endTime = LocalTime.now(),
            address = "测试教室",
            remark = "测试教师",
            startDateStr = "测试开始时间",
            endDateStr = "测试结束时间",
        )
    ScheduleDetail(item)
}

@Composable
fun ScheduleDetail(scheduleItem: ScheduleItem?) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (scheduleItem != null) {
            Text(
                text = scheduleItem.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center,
            )
            Text(scheduleItem.remark, modifier = Modifier.padding(bottom = 10.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
            Text(
                stringResource(R.string.classroom_location, scheduleItem.address),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
            Text(
                stringResource(R.string.start_time, scheduleItem.startDateStr),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
            Text(
                stringResource(R.string.end_time, scheduleItem.endDateStr),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
        }
    }
}
