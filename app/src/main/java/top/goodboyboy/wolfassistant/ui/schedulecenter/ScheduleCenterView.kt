package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.ui.components.TopBarConstants
import top.goodboyboy.wolfassistant.ui.event.TopBarTitleEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCenterView(
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    viewModel: ScheduleCenterViewModel,
    globalEventBus: GlobalEventBus,
) {
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val tabs = listOf("普通课表", "实验课表")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> "普通课表"
            else -> {
                globalEventBus.emit(
                    TopBarTitleEvent(
                        targetTag = TopBarConstants.TOP_BAR_TAG,
                        title = "实验课表",
                    ),
                )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
    ) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> {
                    // 普通课表
                    ScheduleView(viewModel = viewModel, globalEventBus = globalEventBus)
                }

                1 -> {
                    // 实验课表
                    LabScheduleView(viewModel = viewModel)
                }
            }
        }
    }
}
