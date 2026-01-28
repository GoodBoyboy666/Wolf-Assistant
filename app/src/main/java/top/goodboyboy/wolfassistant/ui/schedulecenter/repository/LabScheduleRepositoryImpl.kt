package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import kotlinx.coroutines.flow.first
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleRemoteDataSource

class LabScheduleRepositoryImpl(
    private val labScheduleRemoteDataSource: LabScheduleRemoteDataSource,
    private val labScheduleCacheDataSource: LabScheduleCacheDataSource,
    private val settingsRepository: SettingsRepository,
) : LabScheduleRepository {
    override suspend fun getLabSchedule(week: Int): LabScheduleRepository.LabScheduleData {
        val cache = labScheduleCacheDataSource.getLabScheduleCache(week)
        when (cache) {
            is LabScheduleCacheDataSource.LabScheduleResult.Error -> {
                return LabScheduleRepository.LabScheduleData.Failed(cache.error)
            }

            is LabScheduleCacheDataSource.LabScheduleResult.Success -> {
                return LabScheduleRepository.LabScheduleData.Success(cache.data)
            }
            LabScheduleCacheDataSource.LabScheduleResult.NoCache -> {
            }
        }
        val data =
            labScheduleRemoteDataSource.getLabSchedule(
                settingsRepository.userIDFlow.first(),
                settingsRepository.getUserPasswordDecrypted(),
            )
        return when (data) {
            is LabScheduleRemoteDataSource.LabScheduleDataResult.Error -> {
                LabScheduleRepository.LabScheduleData.Failed(data.error)
            }
            is LabScheduleRemoteDataSource.LabScheduleDataResult.Success -> {
                labScheduleCacheDataSource.saveLabScheduleCache(data.data)
                LabScheduleRepository.LabScheduleData.Success(data.data[week] ?: emptyList())
            }
        }
    }

    override suspend fun cleanLabScheduleCache() {
        labScheduleCacheDataSource.cleanLabScheduleCache()
    }
}
