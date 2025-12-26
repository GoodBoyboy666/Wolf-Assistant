package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleCenterViewModel.LoadScheduleState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScheduleView(viewModel: ScheduleCenterViewModel) {
    val loadScheduleState by viewModel.loadLabScheduleState.collectAsStateWithLifecycle()
    val labScheduleList by viewModel.labScheduleList.collectAsStateWithLifecycle()
    val selectWeekNum by viewModel.weekNumber.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectWeekNum) {
        viewModel.loadLabScheduleList()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = "第 $selectWeekNum 周",
                onValueChange = {},
                readOnly = true,
                label = { Text("选择周数") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                showSheet = true
                            }
                        },
                enabled = false,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSheet = false
                    },
                    sheetState = sheetState,
                ) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .padding(vertical = 10.dp),
                    ) {
                        items(20) { index ->
                            ListItem(
                                headlineContent = { Text("第 ${index + 1} 周") },
                                leadingContent = {
                                    if (index + 1 == selectWeekNum) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                },
                                modifier =
                                    Modifier
                                        .clickable {
                                            scope.launch {
                                                viewModel.setSelectedWeek(index + 1)
                                                sheetState.hide()
                                                showSheet = false
                                            }
                                        },
                                colors =
                                    ListItemDefaults.colors(
                                        containerColor = Color.Transparent,
                                    ),
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var isRefreshing by remember { mutableStateOf(false) }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        viewModel.cleanLabCache()
                        viewModel.loadLabScheduleList()
                    }
                    isRefreshing = false
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (loadScheduleState) {
                    LoadScheduleState.Idle, LoadScheduleState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingCompose("加载实验课表中...")
                        }
                    }

                    is LoadScheduleState.Success -> {
                        LabScheduleCompose(labScheduleList, Modifier.padding(10.dp))
                    }

                    is LoadScheduleState.Failed -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (loadScheduleState as LoadScheduleState.Failed).message,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
