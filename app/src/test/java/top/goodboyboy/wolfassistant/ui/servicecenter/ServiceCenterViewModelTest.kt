package top.goodboyboy.wolfassistant.ui.servicecenter

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.ServiceCenterViewModel.LoadServiceState
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.TokenKeyName
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository

class ServiceCenterViewModelTest {
    private val serviceRepository: ServiceRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var viewModel: ServiceCenterViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadService() =
        runTest {
            val accessToken = "test"
            val tokenKeyName =
                TokenKeyName(
                    headerTokenKeyName = "testHeaderTokenKeyName",
                    urlTokenKeyName = "testUrlTokenKeyName",
                )
            val serviceList =
                listOf(
                    ServiceItem(
                        imageUrl = "testImageUrl",
                        text = "testtext",
                        serviceUrl = "testServiceUrl",
                        tokenAccept = tokenKeyName,
                    ),
                )
            val data = ServiceRepository.ServiceListData.Success(serviceList)
            every { settingsRepository.accessTokenFlow } returns flowOf("test")
            coEvery { serviceRepository.getServiceList(accessToken) } returns data
            viewModel.loadService()
            assertEquals(serviceList, viewModel.serviceList.value)
            assertEquals(LoadServiceState.Success, viewModel.loadServiceState.value)
        }

    @Test
    fun cleanServiceList() {
    }

    @Test
    fun changeLoadServiceState() {
    }
}
