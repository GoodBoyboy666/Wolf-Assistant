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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

@Preview
@Composable
fun LabScheduleDetailPreview() {
    val item =
        LabScheduleItem(
            courseName = "测试课程",
            courseCode = "测试Code",
            className = "测试教室",
            location = "测试地点",
            section = "测试节次",
        )
    LabScheduleDetail(item)
}

@Composable
fun LabScheduleDetail(scheduleItem: LabScheduleItem?) {
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
                text = scheduleItem.courseName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center,
            )
            Text("实验课程", modifier = Modifier.padding(bottom = 10.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))
            Text(
                "课程编号: ${scheduleItem.courseCode}",
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
            Text(
                "班级: ${scheduleItem.className}",
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
            Text(
                "地点: ${scheduleItem.location}",
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
            Text(
                "节次: ${scheduleItem.section}",
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(2.dp),
            )
        }
    }
}
