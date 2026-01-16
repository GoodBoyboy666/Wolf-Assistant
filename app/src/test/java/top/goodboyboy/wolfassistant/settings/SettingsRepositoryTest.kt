package top.goodboyboy.wolfassistant.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.util.CryptoManager

/**
 * SettingsRepository 的单元测试类
 * 用于验证设置存储库的各项功能，包括数据的读取、写入以及清除操作
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var cryptoManager: CryptoManager
    private lateinit var settingsRepository: SettingsRepository

    // 使用 MutableStateFlow 模拟 DataStore 的数据流
    private val preferencesFlow = MutableStateFlow(emptyPreferences())

    /**
     * 测试前的初始化工作
     * 模拟 DataStore 的行为，使其能够在内存中读写数据
     */
    @BeforeEach
    fun setup() {
        preferencesFlow.value = emptyPreferences()
        dataStore = mockk()
        cryptoManager = mockk()

        // 模拟 dataStore.data 返回 flow
        every { dataStore.data } returns preferencesFlow

        // 模拟 updateData 操作，更新 flow 的值并返回新值
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            val current = preferencesFlow.value
            val new = transform(current)
            preferencesFlow.value = new
            new
        }

        every { cryptoManager.encrypt(any()) } answers { "encrypted_" + firstArg<String>() }
        every { cryptoManager.decrypt(any()) } answers {
            val input = firstArg<String>()
            if (input.startsWith("encrypted_")) {
                input.removePrefix("encrypted_")
            } else {
                input
            }
        }

        settingsRepository = SettingsRepository(dataStore, cryptoManager)
    }

    /**
     * 验证默认值是否正确
     * 确保在没有写入任何数据时，各配置项返回预期的默认值
     */
    @Test
    fun `default values are correct`() =
        runTest {
            assertFalse(settingsRepository.darkModeFlow.first())
            assertEquals("", settingsRepository.userNameFlow.first())
            assertEquals("", settingsRepository.userIDFlow.first())
            assertEquals("", settingsRepository.userOrganization.first())
            // assertEquals("", settingsRepository.accessTokenFlow.first()) // Removed
            assertEquals("", settingsRepository.getAccessTokenDecrypted())
            assertEquals("", settingsRepository.getUserPasswordDecrypted())
            assertFalse(settingsRepository.disableSSLCertVerification.first())
            assertFalse(settingsRepository.onlyIPv4.first())
        }

    /**
     * 验证暗黑模式设置能否正确更新 Flow
     */
    @Test
    fun `setDarkMode updates flow`() =
        runTest {
            settingsRepository.setDarkMode(true)
            assertTrue(settingsRepository.darkModeFlow.first())
        }

    /**
     * 验证用户名设置能否正确更新 Flow
     */
    @Test
    fun `setUserName updates flow`() =
        runTest {
            val name = "Test User"
            settingsRepository.setUserName(name)
            assertEquals(name, settingsRepository.userNameFlow.first())
        }

    /**
     * 验证用户ID设置能否正确更新 Flow
     */
    @Test
    fun `setUserID updates flow`() =
        runTest {
            val id = "12345"
            settingsRepository.setUserID(id)
            assertEquals(id, settingsRepository.userIDFlow.first())
        }

    /**
     * 验证加密的用户密码能否正确存储和解密
     */
    @Test
    fun `setUserPasswordEncrypted stores encrypted and decrypts correctly`() =
        runTest {
            val password = "MySecretPassword"
            settingsRepository.setUserPasswordEncrypted(password)
            assertEquals(password, settingsRepository.getUserPasswordDecrypted())
        }

    /**
     * 验证加密的访问令牌能否正确存储和解密
     */
    @Test
    fun `setAccessTokenEncrypted stores encrypted and decrypts correctly`() =
        runTest {
            val token = "token_abc"
            settingsRepository.setAccessTokenEncrypted(token)
            assertEquals(token, settingsRepository.getAccessTokenDecrypted())
        }

    /**
     * 验证用户组织设置能否正确更新 Flow
     */
    @Test
    fun `setUserOrganization updates flow`() =
        runTest {
            val org = "My Org"
            settingsRepository.setUserOrganization(org)
            assertEquals(org, settingsRepository.userOrganization.first())
        }

    /**
     * 验证 SSL 证书校验设置能否正确更新 Flow
     */
    @Test
    fun `setSSLCertVerification updates flow`() =
        runTest {
            settingsRepository.setSSLCertVerification(true)
            assertTrue(settingsRepository.disableSSLCertVerification.first())
        }

    /**
     * 验证仅 IPv4 设置能否正确更新 Flow
     */
    @Test
    fun `setOnlyIPv4 updates flow`() =
        runTest {
            settingsRepository.setOnlyIPv4(true)
            assertTrue(settingsRepository.onlyIPv4.first())
        }

    /**
     * 验证清除所有数据功能是否有效
     * 确保执行 cleanAllData 后，所有设置项恢复为默认值
     */
    @Test
    fun `cleanAllData clears all preferences`() =
        runTest {
            settingsRepository.setDarkMode(true)
            settingsRepository.setUserName("User")
            settingsRepository.setAccessTokenEncrypted("Token")

            settingsRepository.cleanAllData()

            assertFalse(settingsRepository.darkModeFlow.first())
            assertEquals("", settingsRepository.userNameFlow.first())
            assertEquals("", settingsRepository.getAccessTokenDecrypted())
        }

    /**
     * 验证清除用户数据功能是否只清除用户相关信息
     * 确保执行 cleanUser 后，用户相关数据被清除，但系统设置（如暗黑模式）保持不变
     */
    @Test
    fun `cleanUser clears only user related preferences`() =
        runTest {
            // 设置用户数据
            settingsRepository.setUserName("User")
            settingsRepository.setUserID("123")
            // settingsRepository.setAccessToken("Token") // Removed
            settingsRepository.setUserOrganization("Org")

            // 设置其他数据
            settingsRepository.setDarkMode(true)
            settingsRepository.setSSLCertVerification(true)

            settingsRepository.cleanUser()

            // 用户数据应该被清除（变回默认值）
            assertEquals("", settingsRepository.userNameFlow.first())
            assertEquals("", settingsRepository.userIDFlow.first())
            // assertEquals("", settingsRepository.accessTokenFlow.first()) // Removed
            assertEquals("", settingsRepository.userOrganization.first())

            // 其他数据应该保留
            assertTrue(settingsRepository.darkModeFlow.first())
            assertTrue(settingsRepository.disableSSLCertVerification.first())
        }
}
