package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ScheduleRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: ScheduleAPIService,
        private val logger: AppLogger,
    ) : ScheduleRemoteDataSource {
        override suspend fun getSchedule(
            accessToken: String,
            startDate: LocalDate,
            endDate: LocalDate,
        ): DataResult {
            try {
                logger.tag("ScheduleRemote").i("获取课表远程数据: $startDate ~ $endDate")
                val timeList = listOf("08:00:00", "10:00:00", "14:00:00", "16:00:00", "19:00:00", "21:00:00")
                val response =
                    apiService.getSchedule(
                        accessToken = accessToken,
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                    )

                response.use { body ->
                    val jsonElement =
                        JsonParser.parseString(body.string()).asJsonObject.get("data")
                    if (jsonElement.isJsonNull) {
                        val numDays = (endDate.toEpochDay() - startDate.toEpochDay() + 1).toInt()
                        return DataResult.Success(List(numDays) { List(timeList.size) { null } })
                    }

                    val schedule =
                        jsonElement.asJsonObject
                            .get("schedule")
                            .asJsonObject
                            .asMap()
                    val list = mutableListOf<List<ScheduleItem?>>()
                    generateSequence(startDate) { it.plusDays(1) }
                        .takeWhile { it <= endDate }
                        .forEach { date ->
                            val currentDateSchedule = schedule[date.toString()]
                            if (currentDateSchedule == null) {
                                list.add(List(timeList.size) { null })
                                return@forEach
                            }
                            val calendarList = currentDateSchedule.asJsonObject.get("calendarList").asJsonArray
                            val calendarMap = mutableMapOf<String, ScheduleItem>()
                            calendarList.forEach {
                                val calendar = it.asJsonObject
                                val startTime = calendar.get("startTime").asString
                                val scheduleItem =
                                    ScheduleItem(
                                        title = calendar.get("title")?.asString ?: "",
                                        startDate = OffsetDateTime.parse(calendar.get("startDate").asString),
                                        startTime = LocalTime.parse(startTime),
                                        endDate = OffsetDateTime.parse(calendar.get("endDate").asString),
                                        endTime = LocalTime.parse(calendar.get("endTime").asString),
                                        address = calendar.get("address").asString,
                                        remark = calendar.get("remark").asString,
                                        startDateStr = calendar.get("startDateStr").asString,
                                        endDateStr = calendar.get("endDateStr").asString,
                                    )
                                calendarMap[startTime] = scheduleItem
                            }
                            list.add(timeList.map { calendarMap[it] })
                        }
                    logger.tag("ScheduleRemote").i("获取课表远程数据成功")
                    return DataResult.Success(list)
                }
            } catch (e: HttpException) {
                logger.e(e, "获取课表远程数据时发生Http异常")
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "获取课表远程数据时发生Json解析异常")
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "请求课表时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取课表远程数据时发生IO异常")
                return DataResult.Error(Failure.IOError("请求课表时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                logger.e(e, "获取课表远程数据时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }
    }
