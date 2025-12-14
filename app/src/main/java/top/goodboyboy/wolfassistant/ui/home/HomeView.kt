package top.goodboyboy.wolfassistant.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.CurrencyYen
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.R
import java.net.URLEncoder

@Preview
@Composable
private fun HomeViewPreview() {
    HomeView(PaddingValues(), rememberNavController(), hiltViewModel())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: HomeViewModel,
) {
    val name by viewModel.userName.collectAsState(stringResource(R.string.friends))
    val timeTalk by viewModel.timeTalk.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
    ) {
        OutlinedCard(
            modifier =
                Modifier
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp)
                    .fillMaxWidth(),
        ) {
            Text("亲爱的$name，\n\t\t\t\t$timeTalk", modifier = Modifier.padding(10.dp))
        }
        OutlinedCard(
            modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .clip(shape)
                            .clickable(onClick = {
                                navController.navigate("scanner")
                            }),
                ) {
                    Icon(
                        Icons.Rounded.QrCodeScanner,
                        stringResource(R.string.scan),
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.scan), color = MaterialTheme.colorScheme.primary)
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .clip(shape)
                            .clickable(onClick = {
                                val url =
                                    "https://v8mobile.hut.edu.cn/zdRedirect/toSingleMenu?code=openVirtualcard"
                                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                                val urlTokenKeyName = "X-Id-Token"
                                navController.navigate("browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName")
                            }),
                ) {
                    Icon(
                        Icons.Rounded.QrCode,
                        stringResource(R.string.payment_code),
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.payment_code), color = MaterialTheme.colorScheme.primary)
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .clip(shape)
                            .clickable(onClick = {
                                val url =
                                    "https://hub.17wanxiao.com/bsacs/light.action?flag=supwisdomapp_hngydxsw&ecardFunc=recharge"
                                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                                val urlTokenKeyName = "token"
                                navController.navigate("browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName")
                            }),
                ) {
                    Icon(
                        Icons.Rounded.CurrencyYen,
                        stringResource(R.string.recharge),
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.recharge), color = MaterialTheme.colorScheme.primary)
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .clip(shape)
                            .clickable(onClick = {
                                val url = "https://v8mobile.hut.edu.cn/homezzdx/openHomePage"
                                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                                val urlTokenKeyName = "X-Id-Token"
                                navController.navigate("browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName")
                            }),
                ) {
                    Icon(
                        Icons.Rounded.CreditCard,
                        stringResource(R.string.campus_card),
                        modifier =
                            Modifier
                                .padding(10.dp)
                                .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.campus_card), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        val portalState by viewModel.portalState.collectAsStateWithLifecycle()
        val portalCategories = viewModel.portalCategoryList.collectAsStateWithLifecycle()
        val portalInfoList = viewModel.portalInfoList.collectAsStateWithLifecycle()
        val pagerState =
            rememberPagerState(
                initialPage = 0,
                pageCount = { portalCategories.value.size },
            )
        val scope = rememberCoroutineScope()

        OutlinedCard(
            modifier =
                Modifier
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp)
                    .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (portalState) {
                    is HomeViewModel.PortalState.Failed -> {
                        Text(stringResource(R.string.load_fail))
                        Text(
                            stringResource(R.string.reason) + (portalState as HomeViewModel.PortalState.Failed).message,
                        )
                    }

                    HomeViewModel.PortalState.Idle -> {
                        Text(stringResource(R.string.watting_for_loading))
                    }

                    HomeViewModel.PortalState.Success -> {
                        PrimaryTabRow(
                            modifier = Modifier.padding(10.dp),
                            selectedTabIndex = pagerState.currentPage,
                        ) {
                            portalCategories.value.forEachIndexed { index, portalCategory ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(text = portalCategory.portalName) },
                                )
                            }
                        }
                        var isRefreshing by remember { mutableStateOf(false) }
                        HorizontalPager(
                            modifier =
                                Modifier
                                    .padding(10.dp)
                                    .fillMaxSize(),
                            state = pagerState,
                            verticalAlignment = Alignment.Top,
                        ) { index ->
                            val infos = portalInfoList.value[index]
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    viewModel.changePortalState(HomeViewModel.PortalState.Loading)
                                    scope.launch {
                                        viewModel.cleanPortal()
                                        withContext(Dispatchers.IO) {
                                            viewModel.loadPortalCategories()
                                            viewModel.loadPortalInfo()
                                        }
                                    }
                                    isRefreshing = false
                                },
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                                ) {
                                    items(infos) { info ->
                                        InfoItem(info) { url ->
                                            val encodedUrl = URLEncoder.encode(url, "UTF-8")
                                            navController.navigate("browser/$encodedUrl")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HomeViewModel.PortalState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(bottom = 10.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(stringResource(R.string.on_the_way))
                    }
                }
            }
        }
    }
}
