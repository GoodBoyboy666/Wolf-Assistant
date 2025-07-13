package top.goodboyboy.hutassistant.ui.schedulecenter.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource.DataResult
import top.goodboyboy.hutassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ScheduleRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: ScheduleAPIService,
    ) : ScheduleRemoteDataSource {
        override suspend fun getSchedule(
            accessToken: String,
            startDate: LocalDate,
            endDate: LocalDate,
        ): DataResult {
            try {
                val response =
                    apiService.getSchedule(
                        accessToken = accessToken,
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                    )
                response.use {
                    val jsonElement =
                        JsonParser.parseString(it.string()).asJsonObject.get("data")
                    if (jsonElement.isJsonNull) {
                        return DataResult.Success(List(35) { null })
                    }

                    val schedule =
                        jsonElement.asJsonObject
                            .get("schedule")
                            .asJsonObject
                            .asMap()
                    val list = mutableListOf<ScheduleItem?>()
                    val timeList = listOf("08:00:00", "10:00:00", "14:00:00", "16:00:00", "19:00:00")
                    generateSequence(startDate) { it.plusDays(1) }
                        .takeWhile { it <= endDate }
                        .forEach {
                            val date = schedule[it.toString()]
                            if (date == null) {
                                val emptyDay = List(5) { null }
                                list.addAll(emptyDay)
                                return@forEach
                            }
                            val calendarList = date.asJsonObject.get("calendarList").asJsonArray
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
                            timeList.forEach {
                                val calendar = calendarMap[it]
                                if (calendar == null) {
                                    list.add(null)
                                } else {
                                    list.add(calendar)
                                }
                            }
                        }
                    return DataResult.Success(list)
                }
            } catch (e: HttpException) {
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: JsonParseException) {
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "请求课表时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return DataResult.Error(Failure.IOError("请求课表时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                return DataResult.Error(Failure.UnknownError(e))
            }
        }
    }
