package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.components.TopBarConstants
import top.goodboyboy.wolfassistant.ui.event.TopBarTitleEvent
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleCenterViewModel.LoadScheduleState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleView(
    viewModel: ScheduleCenterViewModel,
    globalEventBus: GlobalEventBus,
) {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
//    var firstDay by viewModel.firstDay.collectAsStateWithLifecycle()
//    var lastDay by viewModel.lastDay.collectAsStateWithLifecycle()
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    var selection by remember { mutableStateOf(currentDate) }
    val state =
        rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
            firstDayOfWeek = firstDayOfWeek,
        )
    val scope = rememberCoroutineScope()
    var showSchedule by remember { mutableStateOf(false) }
    val loadScheduleState by viewModel.loadScheduleState.collectAsStateWithLifecycle()
    val scheduleList by viewModel.scheduleList.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { scrolling -> !scrolling }
            .map {
                state.firstVisibleWeek
            }.distinctUntilChanged()
            .collect { week ->
                val first = week.days.first().date
                val last = week.days.last().date
                viewModel.setFirstAndLastDay(first, last)
                viewModel.loadScheduleList()
                globalEventBus.emit(
                    TopBarTitleEvent(
                        targetTag = TopBarConstants.TOP_BAR_TAG,
                        title = "${first.year}年${first.monthValue}月",
                    ),
                )
//                Log.d(
//                    "Calendar",
//                    "Scroll finished. Loading data for week starting on: $first ~ $last"
//                )
            }
    }
    LaunchedEffect(Unit) {
        viewModel.rollBackToCurrentDateEvent.collect { _ ->
            state.animateScrollToWeek(currentDate)
        }
    }
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth(),
        ) {
            WeekCalendar(
                state = state,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                dayContent = { day ->
                    Day(day.date, isSelected = selection == day.date) { clicked ->
//                    if (selection != clicked) {
//                        selection = clicked
//                    }
                    }
                },
            )
        }

        when (loadScheduleState) {
            is LoadScheduleState.Failed -> {
                showSchedule = true
            }

            LoadScheduleState.Idle -> {
            }

            LoadScheduleState.Loading -> {
                showSchedule = false
            }

            LoadScheduleState.Success -> {
                showSchedule = true
            }
        }

        // 课表内容
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showSchedule) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            viewModel.cleanCache()
                            viewModel.loadScheduleList()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ScheduleCompose(
                        scheduleList,
                        Modifier.padding(10.dp),
                    )
                }
            } else {
                LoadingCompose()
            }
        }
    }
}

@Composable
private fun Day(
    date: LocalDate,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit,
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd")
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.displayText(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isSelected) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomCenter),
            )
        }
    }
}

fun DayOfWeek.displayText(
    uppercase: Boolean = false,
    narrow: Boolean = false,
): String {
    val style = if (narrow) TextStyle.NARROW else TextStyle.SHORT
    return getDisplayName(style, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}
