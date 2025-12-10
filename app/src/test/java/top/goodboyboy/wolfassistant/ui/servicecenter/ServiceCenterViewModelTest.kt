package top.goodboyboy.wolfassistant.ui.servicecenter

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
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
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
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
    fun `loadService success should update serviceList and set Success state`() = runTest(testDispatcher) {
        // Arrange
        val token = "test-token"
        val mockData = listOf(mockk<ServiceItem>())
        
        coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
        coEvery { serviceRepository.getServiceList(token) } returns 
            ServiceRepository.ServiceListData.Success(mockData)

        viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository)

        // Act
        viewModel.loadService()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.loadServiceState.value is ServiceCenterViewModel.LoadServiceState.Success)
        assertEquals(mockData, viewModel.serviceList.value)
        coVerify(exactly = 1) { serviceRepository.getServiceList(token) }
    }

    /**
     * 测试：加载服务列表失败
     * 预期：状态变为 Failed，且包含错误信息
     */
    @Test
    fun `loadService failure should set Failed state with error message`() = runTest(testDispatcher) {
        // Arrange
        val token = "test-token"
        val errorMsg = "Network Error"
        
        coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
        coEvery { serviceRepository.getServiceList(token) } returns 
            ServiceRepository.ServiceListData.Failed(Failure.IOError(errorMsg, null))

        viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository)

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
    fun `cleanServiceList should clear list and call repository clean`() = runTest(testDispatcher) {
        // Arrange
        viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository)
        
        // Act
        viewModel.cleanServiceList()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.serviceList.value.isEmpty())
        coVerify(exactly = 1) { serviceRepository.cleanServiceList() }
    }

    /**
     * 测试：手动更改状态
     * 预期：loadServiceState 更新为指定状态
     */
    @Test
    fun `changeLoadServiceState should update state`() = runTest(testDispatcher) {
        // Arrange
        viewModel = ServiceCenterViewModel(serviceRepository, settingsRepository)
        val newState = ServiceCenterViewModel.LoadServiceState.Loading

        // Act
        viewModel.changeLoadServiceState(newState)
        
        // Assert
        assertEquals(newState, viewModel.loadServiceState.value)
    }
}