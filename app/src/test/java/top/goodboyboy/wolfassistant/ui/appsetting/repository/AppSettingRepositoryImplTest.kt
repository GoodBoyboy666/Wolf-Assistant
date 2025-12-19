package top.goodboyboy.wolfassistant.ui.appsetting.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.util.VersionUpdateChecker

/**
 * AppSettingRepositoryImpl 的单元测试类
 * 用于验证应用设置仓库实现的逻辑，特别是版本更新检查功能的委托调用
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingRepositoryImplTest {
    private lateinit var versionUpdateChecker: VersionUpdateChecker
    private lateinit var repository: AppSettingRepositoryImpl

    /**
     * 测试前的初始化工作
     * 模拟 VersionUpdateChecker 依赖，并实例化 AppSettingRepositoryImpl
     */
    @BeforeEach
    fun setup() {
        // 创建 VersionUpdateChecker 的 Mock 对象
        versionUpdateChecker = mockk()
        // 注入 Mock 对象创建 Repository 实例
        repository = AppSettingRepositoryImpl(versionUpdateChecker)
    }

    /**
     * 验证 getUpdateInfo 方法是否正确委托给 updateRepository
     *
     * 测试场景：
     * 1. 调用 getUpdateInfo 方法传入旧版本号
     * 2. 验证是否调用了 updateRepository.checkUpdate
     * 3. 验证返回值是否与 updateRepository 返回的结果一致
     */
    @Test
    fun `getUpdateInfo delegates to versionUpdateChecker`() =
        runTest {
            // Arrange (准备)
            val oldVersion = "v1.0.0"
            // 模拟 checkUpdate 返回的预期结果对象
            val expectedResult = mockk<VersionDomainData>()

            // 当调用 updateRepository.checkUpdate 时返回预期结果
            coEvery { versionUpdateChecker.checkUpdate(oldVersion) } returns expectedResult

            // Act (执行)
            val result = repository.getUpdateInfo(oldVersion)

            // Assert (断言)
            // 验证返回结果是否与预期一致
            assertEquals(expectedResult, result)
            // 验证 updateRepository.checkUpdate 是否被正确调用了一次
            coVerify(exactly = 1) { versionUpdateChecker.checkUpdate(oldVersion) }
        }
}
