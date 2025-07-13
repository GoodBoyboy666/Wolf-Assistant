package top.goodboyboy.hutassistant.ui.schedulecenter.model

import java.time.LocalTime
import java.time.OffsetDateTime

data class ScheduleItem(
    val title: String,
    val startDate: OffsetDateTime,
    val startTime: LocalTime,
    val endDate: OffsetDateTime,
    val endTime: LocalTime,
    val address: String,
    val remark: String,
    val startDateStr: String,
    val endDateStr: String,
)
