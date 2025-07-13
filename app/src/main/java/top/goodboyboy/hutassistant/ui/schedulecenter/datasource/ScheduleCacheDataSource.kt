package top.goodboyboy.hutassistant.ui.schedulecenter.datasource

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.schedulecenter.model.ScheduleItem
import java.time.LocalDate

interface ScheduleCacheDataSource {
    /**
     * 获取课表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return DataResult
     */
    suspend fun getSchedule(
        startDate: LocalDate,
        endDate: LocalDate,
    ): DataResult

    /**
     * 缓存课表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param list 课表列表
     * @return SaveResult
     */
    suspend fun saveSchedule(
        startDate: LocalDate,
        endDate: LocalDate,
        list: List<ScheduleItem?>,
    ): SaveResult

    /**
     * 清除课表缓存
     *
     * @return CleanResult
     */
    suspend fun cleanSchedule(): CleanResult

    sealed class DataResult {
        data class Success(
            val list: List<ScheduleItem?>,
        ) : DataResult()

        object NoCache : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }

    sealed class SaveResult {
        object Success : SaveResult()

        data class Error(
            val error: Failure,
        ) : SaveResult()
    }

    sealed class CleanResult {
        object Success : CleanResult()

        data class Error(
            val error: Failure,
        ) : CleanResult()
    }
}
