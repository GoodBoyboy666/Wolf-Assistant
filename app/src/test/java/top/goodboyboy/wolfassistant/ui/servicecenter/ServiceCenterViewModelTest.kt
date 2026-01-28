package top.goodboyboy.wolfassistant.ui.servicecenter

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.SearchRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository

/**
 * ServiceCenterViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证加载服务列表成功场景
 * 2. 验证加载服务列表失败场景
 * 3. 验证清理服务列表功能
 * 4. 验证手动修改状态功能
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServiceCenterViewModelTest {
    private lateinit var viewModel: ServiceCenterViewModel
    private val serviceRepository: ServiceRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val searchRepository: SearchRepository = mockk(relaxed = true)
    private val okHttpClient: OkHttpClient = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Ensure same instance is returned for consistency
        val flow = MutableStateFlow("")
        coEvery { searchRepository.searchQuery } returns flow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 测试：加载服务列表成功
     * 预期：状态变为 Success，serviceList 更新为返回的数据
     */
    @Test
    fun `loadService success should update serviceList and set Success state`() =
        runTest(testDispatcher) {
            // Arrange
            val token = "test-token"
            val mockData =
                listOf(
                    mockk<ServiceItem> {
                        coEvery { text } returns "Test Service"
                    },
                )

            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            coEvery { serviceRepository.getServiceList(token) } returns
                ServiceRepository.ServiceListData.Success(mockData)

            viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository, searchRepository, okHttpClient)

            // Collect to trigger SharingStarted.WhileSubscribed
            val job = launch { viewModel.serviceList.collect {} }
            testDispatcher.scheduler.advanceUntilIdle()

            clearMocks(serviceRepository, answers = false)

            // Act
            viewModel.loadService()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertTrue(viewModel.loadServiceState.value is ServiceCenterViewModel.LoadServiceState.Success)
            assertEquals(mockData, viewModel.serviceList.value)
            coVerify(exactly = 1) { serviceRepository.getServiceList(token) }

            job.cancel()
        }

    /**
     * 测试：加载服务列表失败
     * 预期：状态变为 Failed，且包含错误信息
     */
    @Test
    fun `loadService failure should set Failed state with error message`() =
        runTest(testDispatcher) {
            // Arrange
            val token = "test-token"
            val errorMsg = "Network Error"

            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            coEvery { serviceRepository.getServiceList(token) } returns
                ServiceRepository.ServiceListData.Failed(Failure.IOError(errorMsg, null))

            viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository, searchRepository, okHttpClient)
            testDispatcher.scheduler.advanceUntilIdle()
            clearMocks(serviceRepository, answers = false)

            // Act
            viewModel.loadService()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = viewModel.loadServiceState.value
            assertTrue(state is ServiceCenterViewModel.LoadServiceState.Failed)
            assertEquals(errorMsg, (state as ServiceCenterViewModel.LoadServiceState.Failed).message)
        }

    /**
     * 测试：清理服务列表
     * 预期：serviceList 为空，且调用仓库的清理方法
     */
    @Test
    fun `cleanServiceList should clear list and call repository clean`() =
        runTest(testDispatcher) {
            // Arrange
            val token = "test-token"
            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            coEvery { serviceRepository.getServiceList(token) } returns
                ServiceRepository.ServiceListData.Success(emptyList())

            viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository, searchRepository, okHttpClient)

            // Collect to trigger SharingStarted.WhileSubscribed
            val job = launch { viewModel.serviceList.collect {} }
            testDispatcher.scheduler.advanceUntilIdle()

            // Act
            viewModel.cleanServiceList()
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            assertTrue(viewModel.serviceList.value.isEmpty())
            coVerify(exactly = 1) { serviceRepository.cleanServiceList() }

            job.cancel()
        }

    /**
     * 测试：更新搜索查询
     * 预期：调用 SearchRepository 更新查询
     */
    @Test
    fun `updateQuery should call searchRepository updateQuery`() =
        runTest(testDispatcher) {
            // Arrange
            viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository, searchRepository, okHttpClient)

            // Act
            val query = "test"
            viewModel.updateQuery(query)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { searchRepository.updateQuery(query) }
        }

    /**
     * 测试：搜索过滤功能
     * 预期：根据查询词过滤服务列表
     */
    @Test
    fun `search functionality should filter service list`() =
        runTest(testDispatcher) {
            // Arrange
            val token = "test-token"
            val item1 = mockk<ServiceItem> { coEvery { text } returns "Apple Service" }
            val item2 = mockk<ServiceItem> { coEvery { text } returns "Banana Service" }
            val mockData = listOf(item1, item2)

            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            coEvery { serviceRepository.getServiceList(token) } returns
                ServiceRepository.ServiceListData.Success(mockData)

            val queryFlow = MutableStateFlow("")
            coEvery { searchRepository.searchQuery } returns queryFlow

            viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository, searchRepository, okHttpClient)

            // Collect to trigger SharingStarted.WhileSubscribed
            val job = launch { viewModel.serviceList.collect {} }
            testDispatcher.scheduler.advanceUntilIdle()

            // Act - Initial load
            viewModel.loadService()
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(2, viewModel.serviceList.value.size)

            // Act - Search "Apple"
            queryFlow.value = "Apple"
            testDispatcher.scheduler.advanceTimeBy(200) // Advance time for debounce
            testDispatcher.scheduler.runCurrent()

            // Assert
            assertEquals(1, viewModel.serviceList.value.size)
            assertTrue(viewModel.serviceList.value.contains(item1))

            // Act - Search "Banana"
            queryFlow.value = "Banana"
            testDispatcher.scheduler.advanceTimeBy(200) // Advance time for debounce
            testDispatcher.scheduler.runCurrent()

            // Assert
            assertEquals(1, viewModel.serviceList.value.size)
            assertTrue(viewModel.serviceList.value.contains(item2))

            // Act - Search empty
            queryFlow.value = ""
            testDispatcher.scheduler.advanceTimeBy(200) // Advance time for debounce
            testDispatcher.scheduler.runCurrent()

            // Assert
            assertEquals(2, viewModel.serviceList.value.size)

            job.cancel()
        }
}
