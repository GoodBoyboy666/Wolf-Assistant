package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalDate

interface ScheduleRepository {
    /**
     * 获取课表
     *
     * @param accessToken 令牌
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return ScheduleData
     */
    suspend fun getSchedule(
        accessToken: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): ScheduleData

    /**
     * 清除课表缓存
     *
     */
    suspend fun cleanScheduleCache()

    sealed class ScheduleData {
        data class Success(
            val data: List<ScheduleItem?>,
        ) : ScheduleData()

        data class Failed(
            val error: Failure,
        ) : ScheduleData()
    }
}
