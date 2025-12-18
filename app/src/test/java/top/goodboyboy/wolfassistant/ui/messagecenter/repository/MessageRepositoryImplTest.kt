package top.goodboyboy.wolfassistant.ui.messagecenter.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSource

/**
 * MessageRepositoryImpl 的单元测试
 * 验证消息仓库的功能，包括获取 AppID 和创建消息列表流
 */
class MessageRepositoryImplTest {
    private lateinit var repository: MessageRepositoryImpl
    private val apiService: MessageAPIService = mockk()
    private val messageDataSource: MessageDataSource = mockk()

    @BeforeEach
    fun setup() {
        repository = MessageRepositoryImpl(apiService, messageDataSource)
    }

    /**
     * 测试：getAppID 成功时应返回 AppIDData.Success
     */
    @Test
    fun `getAppID returns Success when dataSource succeeds`() =
        runTest {
            val accessToken = "token"
            val appIDs = listOf("app1", "app2")
            coEvery { messageDataSource.getAppID(accessToken) } returns MessageDataSource.DataResult.Success(appIDs)

            val result = repository.getAppID(accessToken)

            assertTrue(result is MessageRepository.AppIDData.Success)
            assertEquals(appIDs, (result as MessageRepository.AppIDData.Success).data)
        }

    /**
     * 测试：getAppID 失败时应返回 AppIDData.Failed
     */
    @Test
    fun `getAppID returns Failed when dataSource fails`() =
        runTest {
            val accessToken = "token"
            val error = Failure.ApiError(500, "Server Error")
            coEvery { messageDataSource.getAppID(accessToken) } returns MessageDataSource.DataResult.Error(error)

            val result = repository.getAppID(accessToken)

            assertTrue(result is MessageRepository.AppIDData.Failed)
            assertEquals(error, (result as MessageRepository.AppIDData.Failed).reason)
        }

    /**
     * 测试：getMessages 应返回不为空的 Flow
     */
    @Test
    fun `getMessages returns non-null flow`() =
        runTest {
            val flow = repository.getMessages("token", "appID")
            assertNotNull(flow)
        }

    /**
     * 测试：createErrorFlow 应返回不为空的 Flow
     */
    @Test
    fun `createErrorFlow returns non-null flow`() =
        runTest {
            val error = RuntimeException("Test Error")
            val flow = repository.createErrorFlow(error)
            assertNotNull(flow)
        }
}
