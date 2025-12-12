package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        val title =
            when (selectedTab) {
                0 -> "普通课表"
                else -> "实验课表"
            }
        globalEventBus.emit(
            TopBarTitleEvent(
                targetTag = TopBarConstants.TOP_BAR_TAG,
                title = title,
            ),
        )

//        scope.launch {
//            when (selectedTab) {
//                0 -> {
//                    viewModel.loadScheduleList()
//                }
//                1 -> {
//                    viewModel.loadScheduleList()
//                }
//            }
//        }
    }

    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // 普通课表
                ScheduleView(viewModel = viewModel, globalEventBus = globalEventBus)
            }

            1 -> {
                // 实验课表
                LabScheduleView(viewModel = viewModel, globalEventBus = globalEventBus)
            }
        }
    }
}
