package top.goodboyboy.wolfassistant.ui.messagecenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem

@Composable
fun MessageCenterView(
    innerPadding: PaddingValues,
    viewModel: MessageCenterViewModel,
) {
    val messageCategory = viewModel.messageCategory
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { messageCategory.size },
        )
    val scope = rememberCoroutineScope()
    OutlinedCard(
        modifier =
            Modifier
                .padding(innerPadding)
                .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp)
                .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PrimaryTabRow(
                modifier = Modifier.padding(10.dp),
                selectedTabIndex = pagerState.currentPage,
            ) {
                messageCategory.forEachIndexed { index, category ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = category) },
                    )
                }
            }

            HorizontalPager(
                modifier =
                    Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                state = pagerState,
                verticalAlignment = Alignment.Top,
                key = { index -> messageCategory[index] },
            ) { index ->
                val lazyMessageItems =
                    viewModel
                        .getMessagePagingFlow(
                            index,
                        ).collectAsLazyPagingItems()
                MessageList(lazyMessageItems)
            }
        }
    }
}

@Composable
fun MessageList(items: LazyPagingItems<MessageItem>) {
    LazyColumn(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp),
    ) {
        items(
            count = items.itemCount,
//            key = { index -> items.peek(index)?.title ?: index }
        ) { index ->
            items[index]?.let {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        it.editTime,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(10.dp),
                    )
                    MessageCard(it)
                }
            }
        }

        items.loadState.apply {
            when {
                refresh is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingCompose()
                        }
                    }
                }

                refresh is LoadState.Error -> {
                    val e = items.loadState.refresh as LoadState.Error
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.loading_failed, e.error.localizedMessage))
                            Button(onClick = { items.retry() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                append is LoadState.Error -> {
                    val e = items.loadState.append as LoadState.Error
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.load_more_failed, e.error.localizedMessage))
                                Button(onClick = { items.retry() }) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }

                append is LoadState.Loading -> {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
