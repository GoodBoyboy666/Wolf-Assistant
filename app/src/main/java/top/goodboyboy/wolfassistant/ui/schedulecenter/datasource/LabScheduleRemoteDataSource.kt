package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

interface LabScheduleRemoteDataSource {
    suspend fun getLabSchedule(
        username: String,
        password: String,
    ): LabScheduleDataResult

    sealed class LabScheduleDataResult {
        data class Success(
            val data: Map<Int, List<LabScheduleItem?>>,
        ) : LabScheduleDataResult()

        data class Error(
            val error: Failure,
        ) : LabScheduleDataResult()
    }
}
