package top.goodboyboy.wolfassistant.ui.appsetting.datasource

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.github.update.UpdateAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource.VersionDataResult
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionInfo
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
                    val releaseObject = JsonParser.parseString(it.string()).asJsonObject
                    val versionInfo = parseReleaseJson(releaseObject)
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

        override suspend fun checkUpdateInfoIncludePreRelease(): VersionDataResult {
            try {
                val response = apiService.getReleases()
                response.use {
                    val releasesArray = JsonParser.parseString(it.string()).asJsonArray
                    if (releasesArray.isEmpty) {
                        return VersionDataResult.Error(Failure.DataError("未找到任何版本信息"))
                    }
                    val versionInfo = parseReleaseJson(releasesArray[0].asJsonObject)
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

        private fun parseReleaseJson(jsonObject: JsonObject): VersionInfo {
            val versionString = jsonObject.get("tag_name").asString
            val htmlUrl = jsonObject.get("html_url").asString
            val isPrerelease = jsonObject.get("prerelease").asBoolean
            val body = jsonObject.get("body").asString
            return VersionInfo(
                version = versionString,
                htmlUrl = htmlUrl,
                isPrerelease = isPrerelease,
                body = body,
            )
        }
    }
