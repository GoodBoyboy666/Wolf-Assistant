package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

interface LabScheduleCacheDataSource {
    suspend fun getLabScheduleCache(week: Int): LabScheduleResult

    suspend fun saveLabScheduleCache(data: Map<Int, List<LabScheduleItem?>>): SaveLabScheduleResult

    suspend fun cleanLabScheduleCache(): CleanLabScheduleResult

    sealed class LabScheduleResult {
        data class Success(
            val data: List<LabScheduleItem?> = emptyList(),
        ) : LabScheduleResult()

        data class Error(
            val error: Failure,
        ) : LabScheduleResult()

        object NoCache : LabScheduleResult()
    }

    sealed class SaveLabScheduleResult {
        object Success : SaveLabScheduleResult()

        data class Error(
            val error: Failure,
        ) : SaveLabScheduleResult()
    }

    sealed class CleanLabScheduleResult {
        object Success : CleanLabScheduleResult()

        data class Error(
            val error: Failure,
        ) : CleanLabScheduleResult()
    }
}
