package top.goodboyboy.wolfassistant.ui.servicecenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.components.SearchTextField
import top.goodboyboy.wolfassistant.ui.servicecenter.ServiceCenterViewModel.LoadServiceState
import top.goodboyboy.wolfassistant.ui.servicecenter.service.components.ServiceCard
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCenterView(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: ServiceCenterViewModel,
) {
    val loadServiceState by viewModel.loadServiceState.collectAsStateWithLifecycle()
    val serviceList by viewModel.serviceList.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            viewModel.okHttpClient
                        },
                    ),
                )
            }.build()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    viewModel.cleanServiceList()
                    viewModel.loadService()
                }
                isRefreshing = false
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
        ) {
            when (loadServiceState) {
                is LoadServiceState.Idle -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(stringResource(R.string.please_wait))
                    }
                }

                is LoadServiceState.Loading -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LoadingCompose()
                    }
                }

                is LoadServiceState.Failed -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (loadServiceState as LoadServiceState.Failed).message,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                is LoadServiceState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SearchTextField(
                            value = searchQuery,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 0.dp),
                            onValueChange = {
                                scope.launch {
                                    viewModel.updateQuery(it)
                                }
                            },
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 96.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            0
                            items(serviceList) { service ->
                                ServiceCard(
                                    title = service.text,
                                    imgUrl = service.imageUrl,
                                    imageLoader = imageLoader,
                                    onClick = {
                                        val encodeUrl =
                                            URLEncoder.encode(
                                                service.serviceUrl,
                                                "UTF-8",
                                            )
                                        navController.navigate(
                                            "browser/$encodeUrl?headerTokenKeyName=${service.tokenAccept?.headerTokenKeyName ?: ""}&urlTokenKeyName=${service.tokenAccept?.urlTokenKeyName ?: ""}",
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
