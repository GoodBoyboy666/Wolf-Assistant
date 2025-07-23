package top.goodboyboy.hutassistant.ui.appsetting.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import top.goodboyboy.hutassistant.api.github.update.UpdateAPIService
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.appsetting.VersionUtil
import top.goodboyboy.hutassistant.ui.appsetting.datasource.GitHubDataSource.VersionDataResult
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionInfo
import java.io.IOException
import javax.inject.Inject

class GitHubDataSourceImpl
    @Inject
    constructor(
        private val apiService: UpdateAPIService,
    ) : GitHubDataSource {
        override suspend fun checkUpdateInfo(): VersionDataResult {
            try {
                val response = apiService.getLatestVersionInfo()
                response.use {
                    val latestInfo = JsonParser.parseString(it.string()).asJsonObject
                    val versionString = latestInfo.get("tag_name").asString.removePrefix("v")
                    val htmlUrl = latestInfo.get("html_url").asString
                    val isPrerelease = latestInfo.get("prerelease").asBoolean
                    val body = latestInfo.get("body").asString
                    val versionNameItem = VersionUtil.getVersionNameItem(versionString)
                    val versionInfo =
                        VersionInfo(
                            versionNameItem = versionNameItem,
                            htmlUrl = htmlUrl,
                            isPrerelease = isPrerelease,
                            body = body,
                        )
                    return VersionDataResult.Success(versionInfo)
                }
            } catch (e: HttpException) {
                return VersionDataResult.Error(Failure.ApiError(e.code(), e.message, e))
            } catch (e: JsonParseException) {
                return VersionDataResult.Error(
                    Failure.JsonParsingError(
                        "请求时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return VersionDataResult.Error(Failure.IOError("请求时发生IO错误！" + e.message, e))
            } catch (e: Exception) {
                return VersionDataResult.Error(Failure.UnknownError(e))
            }
        }
    }
