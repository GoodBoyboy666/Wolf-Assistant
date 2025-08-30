package top.goodboyboy.wolfassistant.ui.personalcenter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.ui.components.SettingItem
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCenter(
    innerPadding: PaddingValues,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    viewModel: PersonalCenterViewModel,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    val personalInfo by viewModel.personalInfo.collectAsStateWithLifecycle()
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(25.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column {
                    Text(
                        stringResource(R.string.name, personalInfo?.userName ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.academic_number) + "：${personalInfo?.userUid}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.class_name, personalInfo?.organizationName ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        stringResource(R.string.identity, personalInfo?.identityTypeName ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
    ) {
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
        ) {
            when (loadState) {
                is PersonalCenterViewModel.LoadState.Failed -> {
                    Text("出错了……（悲）\n原因：" + (loadState as PersonalCenterViewModel.LoadState.Failed).reason)
                }

                PersonalCenterViewModel.LoadState.Idle -> {
                    Text(stringResource(R.string.please_wait))
                }

                PersonalCenterViewModel.LoadState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(bottom = 10.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(stringResource(R.string.loading_card))
                    }
                }

                PersonalCenterViewModel.LoadState.Success -> {
                    personalInfo?.let {
                        PersonalCard(
                            personalInfo = it,
                            modifier =
                                Modifier.clickable {
                                    showBottomSheet = true
                                },
                        )
                    }
                }
            }
        }
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp, end = 20.dp),
        ) {
            SettingItem(
                title = stringResource(R.string.settings),
                subtitle = stringResource(R.string.app_settings),
                icon = Icons.Rounded.Settings,
            ) {
                navController.navigate("setting")
            }
            SettingItem(
                title = stringResource(R.string.open_source_license),
                subtitle = stringResource(R.string.third_party_open_source_licenses),
                icon = Icons.Rounded.Copyright,
            ) {
                navController.navigate("oss")
            }
        }
    }
}

@Preview
@Composable
private fun PersonalCardPreview() {
    PersonalCard(
        PersonalInfo(
            userUid = "12345678910",
            userName = "testUser",
            organizationName = "计算机1234",
            identityTypeName = "学生",
            imageUrl = "",
        ),
    )
}

@Composable
fun PersonalCard(
    personalInfo: PersonalInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(10.dp),
    ) {
        AsyncImage(
            model = personalInfo.imageUrl,
            contentDescription = stringResource(R.string.avatar),
            modifier =
                Modifier
                    .padding(8.dp)
                    .size(64.dp)
                    .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(personalInfo.userName, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.padding(3.dp))
            Text(personalInfo.userUid)
        }
    }
}
