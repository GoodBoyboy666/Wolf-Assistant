package top.goodboyboy.wolfassistant.ui.messagecenter

import android.app.Application
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepository

/**
 * MessageCenterViewModel 的单元测试
 *
 * 测试要点：
 *  - accessToken 为空时应使用 createErrorFlow（错误流）
 *  - getAppID 返回 Failed 时应使用 createErrorFlow（错误流）
 *  - 不同 category 映射为不同 appID（0 -> "online", 1 -> data[1], other -> ""）
 *  - 相同 category 重复调用应返回相同的 Flow（缓存行为）
 *  - accessToken 变化时应触发 flatMapLatest（即 accessToken 改变会重新调用仓库方法）
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageCenterViewModelTest {
    // 被测试的依赖（使用 MockK 模拟）
    private val messageRepository: MessageRepository = mockk(relaxed = true)
    private val settingsRepository = mockk<top.goodboyboy.wolfassistant.settings.SettingsRepository>(relaxed = true)
    private val application: Application = mockk(relaxed = true)

    // 用于在测试中替换主协程调度器，便于对协程调度进行控制和前进
    private val testDispatcher = StandardTestDispatcher()

    /**
     * 每个测试前的初始化
     *  - 将 Dispatchers.Main 设置为 testDispatcher，确保 ViewModel 中的 viewModelScope 使用可控的调度器
     *  - 对 Application.getString 做默认 stub，避免依赖 Android 资源
     */
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // 默认 application 字符串，避免访问真实资源
        every { application.getString(any()) } returns "测试"
    }

    /**
     * 每个测试后的清理
     *  - 清理 MockK 的全局状态
     *  - 重置 Dispatchers.Main
     */
    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    /**
     * 测试：当 accessToken 为空时，应当使用仓库的 createErrorFlow 返回错误类型的 PagingData 流
     * Given: settingsRepository.accessTokenFlow 发出空字符串
     * When: 调用 getMessagePagingFlow(0)
     * Then: 应调用 messageRepository.createErrorFlow，并且 Flow 有至少一次 emission
     */
    @Test
    fun `accessToken empty should use createErrorFlow`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("")
            val errorFlow = flowOf(TestPagingDataFactory.create(MessageItem("title", "author", "time", "content")))
            coEvery { messageRepository.createErrorFlow(any()) } returns errorFlow

            // When
            val flow =
                MessageCenterViewModel(messageRepository, settingsRepository, application)
                    .getMessagePagingFlow(0)

            // Then: collect 一次以触发上游逻辑并断言仓库方法被调用
            flow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { messageRepository.createErrorFlow(any()) }
        }

    /**
     * 测试：当 getAppID 返回 Failed 时，应使用 createErrorFlow
     * Given: accessToken 非空，但 messageRepository.getAppID 返回 Failed
     * When: 调用 getMessagePagingFlow(0)
     * Then: 应调用 messageRepository.createErrorFlow 并发出 PagingData
     */
    @Test
    fun `getAppID failed should use createErrorFlow`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            coEvery { messageRepository.getAppID("token") } returns
                MessageRepository.AppIDData.Failed(
                    top.goodboyboy.wolfassistant.common.Failure
                        .IOError("err", null),
                )
            coEvery { messageRepository.createErrorFlow(any()) } returns flowOf(TestPagingDataFactory.create())

            // When
            val flow =
                MessageCenterViewModel(messageRepository, settingsRepository, application)
                    .getMessagePagingFlow(0)

            // Then
            flow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { messageRepository.createErrorFlow(any()) }
        }

    /**
     * 测试：category = 0 时应使用固定 appid "online" 并调用 getMessages
     * Given: accessToken 发出 "token"，getAppID 返回成功数据
     * When: 调用 getMessagePagingFlow(0)
     * Then: 应调用 messageRepository.getMessages("token", "online") 并发出 PagingData
     */
    @Test
    fun `category 0 should use online appid and call getMessages`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            val pd = TestPagingDataFactory.create(MessageItem("title", "author", "time", "content"))
            coEvery { messageRepository.getAppID("token") } returns
                MessageRepository.AppIDData.Success(listOf("a0", "a1"))
            coEvery { messageRepository.getMessages("token", "online") } returns flowOf(pd)

            // When
            val flow =
                MessageCenterViewModel(messageRepository, settingsRepository, application)
                    .getMessagePagingFlow(0)

            // Then
            flow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { messageRepository.getMessages("token", "online") }
        }

    /**
     * 测试：category = 1 时应使用 getAppID 返回列表中的索引 1 作为 appid
     * Given: accessToken 发出 "token"，getAppID 返回含多个 appid 的列表
     * When: 调用 getMessagePagingFlow(1)
     * Then: 应调用 messageRepository.getMessages("token", data[1]) 并发出 PagingData
     */
    @Test
    fun `category 1 should use data index 1 appid and call getMessages`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            val pd = TestPagingDataFactory.create()
            coEvery { messageRepository.getAppID("token") } returns
                MessageRepository.AppIDData.Success(listOf("a0", "a1", "a2"))
            coEvery { messageRepository.getMessages("token", "a1") } returns flowOf(pd)

            // When
            val flow =
                MessageCenterViewModel(messageRepository, settingsRepository, application)
                    .getMessagePagingFlow(1)

            // Then
            flow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { messageRepository.getMessages("token", "a1") }
        }

    /**
     * 测试：未知 category（不在 mapping 内）应传空字符串作为 appid
     * Given: accessToken 发出 "token"，getAppID 返回至少一个 appid
     * When: 调用 getMessagePagingFlow(5)（超出已知索引）
     * Then: 应调用 messageRepository.getMessages("token", "")
     */
    @Test
    fun `unknown category should pass empty appid to getMessages`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            val pd = TestPagingDataFactory.create()
            coEvery { messageRepository.getAppID("token") } returns MessageRepository.AppIDData.Success(listOf("a0"))
            coEvery { messageRepository.getMessages("token", "") } returns flowOf(pd)

            // When
            val flow =
                MessageCenterViewModel(messageRepository, settingsRepository, application)
                    .getMessagePagingFlow(5)

            // Then
            flow.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { messageRepository.getMessages("token", "") }
        }

    /**
     * 测试：同一 category 重复调用应返回缓存的 Flow（引用相等）
     * Given: accessToken 发出 "token" 且仓库返回正常流
     * When: 连续两次调用 getMessagePagingFlow(0)
     * Then: 两次返回的 Flow 应为同一实例（缓存命中）
     */
    @Test
    fun `getMessagePagingFlow should cache flow per category`() =
        runTest(testDispatcher) {
            // Given
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            coEvery { messageRepository.getAppID("token") } returns
                MessageRepository.AppIDData.Success(listOf("a0", "a1"))
            coEvery { messageRepository.getMessages(any(), any()) } returns flowOf(TestPagingDataFactory.create())

            // When
            val vm = MessageCenterViewModel(messageRepository, settingsRepository, application)
            val f1 = vm.getMessagePagingFlow(0)
            val f2 = vm.getMessagePagingFlow(0)

            // Then
            assertSame(f1, f2)
        }

    /**
     * 测试：当 accessToken 改变时，应触发 flatMapLatest，从而对新的 token 再次调用 getAppID/getMessages
     * Given: settingsRepository.accessTokenFlow 为 MutableStateFlow，可变；初始值为 t1
     * When: 订阅 getMessagePagingFlow(0) 并将 token 改为 t2
     * Then: messageRepository.getAppID 会至少被调用两次（对 t1 和 t2）
     */
    @Test
    fun `accessToken change should trigger flatMapLatest`() =
        runTest(testDispatcher) {
            // Given accessToken 是 MutableStateFlow，便于在测试中修改它
            val tokenFlow = MutableStateFlow("t1")
            every { settingsRepository.accessTokenFlow } returns tokenFlow
            coEvery { messageRepository.getAppID(any()) } returns
                MessageRepository.AppIDData.Success(listOf("a0", "a1"))
            coEvery { messageRepository.getMessages(any(), any()) } returns flowOf(TestPagingDataFactory.create())

            // When
            val vm = MessageCenterViewModel(messageRepository, settingsRepository, application)
            val flow = vm.getMessagePagingFlow(0)

            // 长期收集以确保能接收到 token 变化后产生的新内层 Flow 的调用
            val job =
                launch {
                    flow.collect { /* 消费以保持订阅 */ }
                }
            testDispatcher.scheduler.advanceUntilIdle()

            // change token
            tokenFlow.value = "t2"
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: getAppID 对不同 token 至少被调用两次
            coVerify(atLeast = 2) { messageRepository.getAppID(any()) }
            job.cancel()
        }
}
