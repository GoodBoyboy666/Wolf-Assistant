package top.goodboyboy.wolfassistant.ui.servicecenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.components.LoadingCompose
import top.goodboyboy.wolfassistant.ui.servicecenter.ServiceCenterViewModel.LoadServiceState
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
    Column(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
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
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(serviceList) { item ->
                                Column(
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                val encodeUrl =
                                                    URLEncoder.encode(
                                                        item.serviceUrl,
                                                        "UTF-8",
                                                    )
                                                navController.navigate(
                                                    "browser/$encodeUrl?headerTokenKeyName=${item.tokenAccept?.headerTokenKeyName ?: ""}&urlTokenKeyName=${item.tokenAccept?.urlTokenKeyName ?: ""}",
                                                )
                                            },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
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
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(context)
                                                .data(item.imageUrl)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .networkCachePolicy(CachePolicy.ENABLED)
                                                .build(),
                                        contentDescription = item.text,
                                        imageLoader = imageLoader,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .fillMaxSize()
                                                .aspectRatio(1f),
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Text(
                                        item.text,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
