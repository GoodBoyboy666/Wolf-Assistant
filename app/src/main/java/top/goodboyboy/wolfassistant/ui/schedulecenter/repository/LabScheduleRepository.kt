package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

interface LabScheduleRepository {
    suspend fun getLabSchedule(week: Int): LabScheduleData

    sealed class LabScheduleData {
        data class Success(
            val data: List<LabScheduleItem?>,
        ) : LabScheduleData()

        data class Failed(
            val error: Failure,
        ) : LabScheduleData()
    }

    suspend fun cleanLabScheduleCache()
}
