package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import okio.IOException
import org.jsoup.Jsoup
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleSSOAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.util.ParseLabScheduleUtil
import top.goodboyboy.wolfassistant.util.RsaPemUtils
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class LabScheduleRemoteDataSourceImpl
    @Inject
    constructor(
        private val ssoApiService: LabScheduleSSOAPIService,
        private val scheduleApiService: LabScheduleAPIService,
        private val logger: AppLogger,
    ) : LabScheduleRemoteDataSource {
        override suspend fun getLabSchedule(
            username: String,
            password: String,
        ): LabScheduleRemoteDataSource.LabScheduleDataResult {
            try {
                logger.tag("LabScheduleRemote").i("开始获取实验课表")
                // 预热 获得bzb_jsxsd以及execution
                val preFlightResponse = scheduleApiService.getPreFlightPage()
                val preFlightBody = preFlightResponse.body()?.string() ?: ""
                val currentUrl =
                    preFlightResponse
                        .raw()
                        .request.url
                        .toString()
                if (currentUrl.contains("jwxt.hut.edu.cn") && !currentUrl.contains("sso.jsp")) {
                    // 已经登录
                    logger.tag("LabScheduleRemote").i("检测到已登录，跳过CAS认证步骤")
                } else if (preFlightBody.contains("window.location") || preFlightBody.contains("self.location")) {
                    // 未登录
                    logger.tag("LabScheduleRemote").i("检测到未登录，开始执行CAS登录流程")
                    val loginPage = ssoApiService.getLoginPage().string()
                    // 解析execution
                    val doc = Jsoup.parse(loginPage)
                    val executionValue = doc.select("input[name=execution]").attr("value")
                    if (executionValue.isEmpty()) {
                        return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                            Failure.HTMLParsingError("无法获取execution"),
                        )
                    }

                    // 加密身份认证所需密码
                    val publicKeyString = ssoApiService.getPublicKey().string()
                    val publicKey = RsaPemUtils.getPublicKeyFromPem(publicKeyString)
                    val encryptedPassword = "__RSA__" + RsaPemUtils.encrypt(password, publicKey)

                    // 执行登录请求
                    val loginResponse =
                        ssoApiService.loginToJwxt(
                            username = username,
                            encryptedPassword = encryptedPassword,
                            execution = executionValue,
                        )

                    if (!loginResponse.isSuccessful) {
                        return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                            Failure.ApiError(
                                loginResponse.code(),
                                loginResponse.errorBody()?.string(),
                            ),
                        )
                    }

                    // 判断是否到达教务系统
                    val finalUrl =
                        loginResponse
                            .raw()
                            .request.url
                            .toString()
                    val isLoginSuccess = finalUrl.contains("jwxt.hut.edu.cn") && !finalUrl.contains("login")
                    if (!isLoginSuccess) {
                        return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                            Failure.CustomError("登录教务系统失败，可能是用户名或密码错误或者系统错误"),
                        )
                    }
                } else {
                    // 异常
                    return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                        Failure.CustomError("未知的跳转地址: $currentUrl"),
                    )
                }

                // 请求课表数据
                logger.tag("LabScheduleRemote").i("请求实验课表数据")
                val labScheduleResponse = scheduleApiService.getLabSchedule().string()

                // 解析实验课表数据
                logger.tag("LabScheduleRemote").i("解析实验课表数据")
                val result = ParseLabScheduleUtil.parseCourseTable(labScheduleResponse)

                logger.tag("LabScheduleRemote").i("获取实验课表数据成功")
                return LabScheduleRemoteDataSource.LabScheduleDataResult.Success(
                    result,
                )
            } catch (e: HttpException) {
                logger.e(e, "获取实验课表时发生Http异常")
                return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取实验课表时发生IO异常")
                return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                    Failure.IOError(
                        "请求课表时出现IO异常" + e.message,
                        e,
                    ),
                )
            } catch (e: ParseLabScheduleUtil.ParseException) {
                logger.e(e, "解析实验课表时发生异常")
                return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(
                    Failure.HTMLParsingError(
                        "解析实验课表时出现异常" + e.message,
                    ),
                )
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                logger.e(e, "获取实验课表时发生未知异常")
                return LabScheduleRemoteDataSource.LabScheduleDataResult.Error(Failure.UnknownError(e))
            }
        }
    }
