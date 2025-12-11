package top.goodboyboy.wolfassistant.ui.login.repository

import android.util.Base64
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import top.goodboyboy.wolfassistant.api.hutapi.user.LoginAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository.UserData
import java.io.IOException

/**
 * LoginRepositoryImpl 的单元测试类
 * 用于验证登录功能的业务逻辑，包括：
 * 1. 成功登录并解析 Token
 * 2. 处理各种异常情况（网络错误、解析错误等）
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginRepositoryImplTest {
    private lateinit var apiService: LoginAPIService
    private lateinit var repository: LoginRepositoryImpl

    @BeforeEach
    fun setup() {
        apiService = mockk()
        repository = LoginRepositoryImpl(apiService)

        // Mock Android Base64，因为 JWT 库依赖它，而单元测试运行在 JVM 上
        mockkStatic(Base64::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 测试登录成功场景
     * 预期：
     * 1. API 返回包含 idToken 的 JSON
     * 2. idToken 被正确解析（通过 Mock Base64.decode 模拟）
     * 3. 返回 UserData.Success，且包含正确的 UserInfo
     */
    @Test
    fun `loginUser success returns UserData Success`() =
        runTest {
            // Arrange
            val header = "header"
            val payload = "payload"
            val signature = "signature"
            val idToken = "$header.$payload.$signature"

            val jsonResponse =
                """
                {
                    "data": {
                        "idToken": "$idToken"
                    }
                }
                """.trimIndent()
            val responseBody = jsonResponse.toResponseBody("application/json".toMediaTypeOrNull())

            // Mock API 调用返回
            coEvery { apiService.loginUser(any(), any(), any(), any(), any(), any(), any()) } returns responseBody

            // Mock Base64.decode 以支持 JWT 解析
            every { Base64.decode(any<String>(), any()) } answers {
                val str = firstArg<String>()
                when (str) {
                    header -> """{"alg":"HS256","typ":"JWT"}""".toByteArray()
                    payload ->
                        """
                        {
                            "sub": "user123",
                            "ATTR_organizationName": "TestOrg",
                            "ATTR_userName": "TestUser"
                        }
                        """.trimIndent().toByteArray()
                    else -> ByteArray(0)
                }
            }

            // Act
            val result = repository.loginUser("user", "pass", "app", "dev", "os", "client")

            // Assert

            assertTrue(result is UserData.Success, "Result should be Success but was $result")
            val data = (result as UserData.Success).data
            assertEquals("user123", data.userID)
            assertEquals("TestOrg", data.userOrganization)
            assertEquals("TestUser", data.userName)
            assertEquals(idToken, data.accessToken)
        }

    /**
     * 测试登录成功但 Token 缺少必要 Claims 的场景
     * 预期：
     * 1. JWT 解析成功，但缺少 sub, org 或 name 字段
     * 2. 返回 UserData.Failed，包含 JsonParsingError
     */
    @Test
    fun `loginUser missing claims returns UserData Failed`() =
        runTest {
            // Arrange
            val header = "header"
            val payload = "payload_missing"
            val signature = "signature"
            val idToken = "$header.$payload.$signature"

            val jsonResponse = """{"data": {"idToken": "$idToken"}}"""
            val responseBody = jsonResponse.toResponseBody("application/json".toMediaTypeOrNull())

            coEvery { apiService.loginUser(any(), any(), any(), any(), any(), any(), any()) } returns responseBody

            // Mock Base64.decode
            every { Base64.decode(any<String>(), any()) } answers {
                val str = firstArg<String>()
                when (str) {
                    header -> """{"alg":"HS256","typ":"JWT"}""".toByteArray()
                    payload -> """{}""".toByteArray() // 空 JSON，缺少 claims
                    else -> ByteArray(0)
                }
            }

            // Act
            val result = repository.loginUser("user", "pass", "app", "dev", "os", "client")

            // Assert
            assertTrue(result is UserData.Failed)
            assertTrue((result as UserData.Failed).error is Failure.JsonParsingError)
        }

    /**
     * 测试 API 返回 HttpException (如 401, 500) 的场景
     * 预期：返回 UserData.Failed，包含 ApiError
     */
    @Test
    fun `loginUser http exception returns UserData Failed`() =
        runTest {
            // Arrange
            val errorResponse =
                Response.error<ResponseBody>(
                    401,
                    "Unauthorized".toResponseBody("text/plain".toMediaTypeOrNull()),
                )
            val exception = HttpException(errorResponse)

            coEvery { apiService.loginUser(any(), any(), any(), any(), any(), any(), any()) } throws exception

            // Act
            val result = repository.loginUser("user", "pass", "app", "dev", "os", "client")

            // Assert
            assertTrue(result is UserData.Failed)
            val failure = (result as UserData.Failed).error
            assertTrue(failure is Failure.ApiError)
            assertEquals(401, (failure as Failure.ApiError).code)
        }

    /**
     * 测试 API 抛出 IOException (如网络断开) 的场景
     * 预期：返回 UserData.Failed，包含 IOError
     */
    @Test
    fun `loginUser io exception returns UserData Failed`() =
        runTest {
            // Arrange
            coEvery { apiService.loginUser(any(), any(), any(), any(), any(), any(), any()) } throws
                IOException("Network error")

            // Act
            val result = repository.loginUser("user", "pass", "app", "dev", "os", "client")

            // Assert
            assertTrue(result is UserData.Failed)
            assertTrue((result as UserData.Failed).error is Failure.IOError)
        }

    /**
     * 测试 JSON 解析异常 (API 返回非 JSON 格式) 的场景
     * 预期：返回 UserData.Failed，包含 JsonParsingError
     */
    @Test
    fun `loginUser malformed json returns UserData Failed`() =
        runTest {
            // Arrange
            val malformedJson = "{ invalid json }"
            val responseBody = malformedJson.toResponseBody("application/json".toMediaTypeOrNull())

            coEvery { apiService.loginUser(any(), any(), any(), any(), any(), any(), any()) } returns responseBody

            // Act
            val result = repository.loginUser("user", "pass", "app", "dev", "os", "client")

            // Assert
            assertTrue(result is UserData.Failed)
            assertTrue((result as UserData.Failed).error is Failure.JsonParsingError)
        }
}
