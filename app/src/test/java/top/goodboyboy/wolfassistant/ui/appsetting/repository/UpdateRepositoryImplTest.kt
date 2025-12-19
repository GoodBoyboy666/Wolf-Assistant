package top.goodboyboy.wolfassistant.ui.appsetting.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionInfo

/**
 * UpdateRepositoryImpl 的单元测试类
 * 用于验证更新检查功能的逻辑
 */
class UpdateRepositoryImplTest {
    private val gitHubDataSource: GitHubDataSource = mockk()
    private val repository = UpdateRepositoryImpl(gitHubDataSource)

    /**
     * 测试场景：当有新版本可用时，checkUpdate 应返回 Success
     * 预期结果：返回 VersionDomainData.Success，且包含新版本信息
     */
    @Test
    fun `checkUpdate returns Success when new version is available`() =
        runTest {
            // 准备数据：当前版本为 1.0.0，最新版本为 1.1.0
            val currentVersion = "1.0.0"
            val latestVersion = "1.1.0"
            val versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = false, body = "update")

            // 模拟 GitHubDataSource 返回成功结果
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)

            // 执行测试
            val result = repository.checkUpdate(currentVersion, false)

            // 验证结果：应该是 Success 且包含最新版本信息
            assertTrue(result is VersionDomainData.Success)
            assertEquals(versionInfo, (result as VersionDomainData.Success).data)
        }

    /**
     * 测试场景：当当前版本比远程版本新时，checkUpdate 应返回 NOUpdate
     * 预期结果：返回 VersionDomainData.NOUpdate
     */
    @Test
    fun `checkUpdate returns NOUpdate when current version is newer`() =
        runTest {
            // 准备数据：当前版本为 1.2.0，最新版本为 1.1.0
            val currentVersion = "1.2.0"
            val latestVersion = "1.1.0"
            val versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = false, body = "update")

            // 模拟 GitHubDataSource 返回成功结果
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)

            // 执行测试
            val result = repository.checkUpdate(currentVersion, false)

            // 验证结果：应该是 NOUpdate
            assertTrue(result is VersionDomainData.NOUpdate)
        }

    /**
     * 测试场景：当当前版本与远程版本一致时，checkUpdate 应返回 NOUpdate
     * 预期结果：返回 VersionDomainData.NOUpdate
     */
    @Test
    fun `checkUpdate returns NOUpdate when versions are equal`() =
        runTest {
            // 准备数据：当前版本为 1.1.0，最新版本为 1.1.0
            val currentVersion = "1.1.0"
            val latestVersion = "1.1.0"
            val versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = false, body = "update")

            // 模拟 GitHubDataSource 返回成功结果
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)

            // 执行测试
            val result = repository.checkUpdate(currentVersion, false)

            // 验证结果：应该是 NOUpdate
            assertTrue(result is VersionDomainData.NOUpdate)
        }

    /**
     * 测试场景：当数据源返回错误时，checkUpdate 应返回 Error
     * 预期结果：返回 VersionDomainData.Error，且包含错误信息
     */
    @Test
    fun `checkUpdate returns Error when data source fails`() =
        runTest {
            // 准备数据
            val currentVersion = "1.0.0"
            val error = Failure.CustomError("Network Error")

            // 模拟 GitHubDataSource 返回错误结果
            coEvery { gitHubDataSource.checkUpdateInfo() } returns GitHubDataSource.VersionDataResult.Error(error)

            // 执行测试
            val result = repository.checkUpdate(currentVersion, false)

            // 验证结果：应该是 Error 且包含错误信息
            assertTrue(result is VersionDomainData.Error)
            assertEquals(error, (result as VersionDomainData.Error).error)
        }

    /**
     * 测试场景：预发布版本比较 (Alpha < Beta < RC < Stable)
     * 1.0.0-beta < 1.0.0-rc -> Success
     * 1.0.0-rc < 1.0.0 -> Success
     * 1.0.0 > 1.0.0-rc -> NOUpdate
     */
    @Test
    fun `checkUpdate handles pre-release versions correctly`() =
        runTest {
            // Case 1: beta < rc (Update available)
            var currentVersion = "1.0.0-beta"
            var latestVersion = "1.0.0-rc"
            var versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            var result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.Success, "1.0.0-beta should update to 1.0.0-rc")

            // Case 2: rc < stable (Update available)
            currentVersion = "1.0.0-rc"
            latestVersion = "1.0.0"
            versionInfo = VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = false, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.Success, "1.0.0-rc should update to 1.0.0")

            // Case 3: stable > rc (No update)
            currentVersion = "1.0.0"
            latestVersion = "1.0.0-rc"
            versionInfo = VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.NOUpdate, "1.0.0 should not update to 1.0.0-rc")
        }

    /**
     * 测试场景：同阶段版本号比较 (beta.1 < beta.2)
     * 1.0.0-beta.1 < 1.0.0-beta.2 -> Success
     * 1.0.0-beta.2 > 1.0.0-beta.1 -> NOUpdate
     */
    @Test
    fun `checkUpdate handles pre-release version numbers correctly`() =
        runTest {
            // Case 1: beta.1 < beta.2 (Update available)
            var currentVersion = "1.0.0-beta.1"
            var latestVersion = "1.0.0-beta.2"
            var versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            var result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.Success, "1.0.0-beta.1 should update to 1.0.0-beta.2")

            // Case 2: beta.2 > beta.1 (No update)
            currentVersion = "1.0.0-beta.2"
            latestVersion = "1.0.0-beta.1"
            versionInfo = VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.NOUpdate, "1.0.0-beta.2 should not update to 1.0.0-beta.1")
        }

    /**
     * 测试场景：跨版本号比较 (Major/Minor 优先于 Suffix)
     * 1.0.0 < 1.1.0-alpha -> Success
     */
    @Test
    fun `checkUpdate prioritizes core version over suffix`() =
        runTest {
            // Case 1: 1.0.0 < 1.1.0-alpha (Update available)
            val currentVersion = "1.0.0"
            val latestVersion = "1.1.0-alpha"
            val versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")
            coEvery { gitHubDataSource.checkUpdateInfo() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)
            val result = repository.checkUpdate(currentVersion, false)
            assertTrue(result is VersionDomainData.Success, "1.0.0 should update to 1.1.0-alpha")
        }

    /**
     * 测试场景：当启用预发布版本时，checkUpdate 应调用 checkUpdateInfoIncludePreRelease
     * 预期结果：调用 checkUpdateInfoIncludePreRelease 并返回正确结果
     */
    @Test
    fun `checkUpdate calls checkUpdateInfoIncludePreRelease when enablePreRelease is true`() =
        runTest {
            // 准备数据
            val currentVersion = "1.0.0"
            val latestVersion = "1.1.0-beta"
            val versionInfo =
                VersionInfo(version = latestVersion, htmlUrl = "url", isPrerelease = true, body = "update")

            // 模拟 GitHubDataSource 返回成功结果
            coEvery { gitHubDataSource.checkUpdateInfoIncludePreRelease() } returns
                GitHubDataSource.VersionDataResult.Success(versionInfo)

            // 执行测试
            val result = repository.checkUpdate(currentVersion, true)

            // 验证结果
            assertTrue(result is VersionDomainData.Success)
            assertEquals(versionInfo, (result as VersionDomainData.Success).data)
        }
}
