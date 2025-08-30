package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalDate

interface ScheduleRemoteDataSource {
    /**
     * 获取课表
     *
     * @param accessToken 令牌
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return DataResult
     */
    suspend fun getSchedule(
        accessToken: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): DataResult

    sealed class DataResult {
        data class Success(
            val data: List<ScheduleItem?>,
        ) : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }
}
