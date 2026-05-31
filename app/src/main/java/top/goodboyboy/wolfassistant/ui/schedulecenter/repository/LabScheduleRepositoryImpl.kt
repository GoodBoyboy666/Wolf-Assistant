package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import kotlinx.coroutines.flow.first
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabScheduleRepositoryImpl
    @Inject
    constructor(
    private val labScheduleRemoteDataSource: LabScheduleRemoteDataSource,
    private val labScheduleCacheDataSource: LabScheduleCacheDataSource,
    private val settingsRepository: SettingsRepository,
    private val logger: AppLogger,
) : LabScheduleRepository {
    override suspend fun getLabSchedule(
        week: Int,
        forceRefresh: Boolean,
    ): LabScheduleRepository.LabScheduleData {
        logger.i("获取实验课表: 第${week}周")
        if (!forceRefresh) {
            val cache = labScheduleCacheDataSource.getLabScheduleCache(week)
            when (cache) {
                is LabScheduleCacheDataSource.LabScheduleResult.Error -> {
                    logger.i("获取实验课表: 缓存未命中，请求远程数据")
                    return LabScheduleRepository.LabScheduleData.Failed(cache.error)
                }

                is LabScheduleCacheDataSource.LabScheduleResult.Success -> {
                    logger.i("获取实验课表: 缓存命中")
                    return LabScheduleRepository.LabScheduleData.Success(cache.data)
                }
                LabScheduleCacheDataSource.LabScheduleResult.NoCache -> {
                    logger.i("获取实验课表: 缓存未命中，请求远程数据")
                }
            }
        } else {
            logger.i("获取实验课表: 强制刷新，跳过缓存")
        }
        val data =
            labScheduleRemoteDataSource.getLabSchedule(
                settingsRepository.userIDFlow.first(),
                settingsRepository.getUserPasswordDecrypted(),
            )
        return when (data) {
            is LabScheduleRemoteDataSource.LabScheduleDataResult.Error -> {
                logger.e(data.error.cause, "获取实验课表失败")
                LabScheduleRepository.LabScheduleData.Failed(data.error)
            }
            is LabScheduleRemoteDataSource.LabScheduleDataResult.Success -> {
                logger.i("获取实验课表: 远程数据获取成功")
                labScheduleCacheDataSource.saveLabScheduleCache(data.data)
                LabScheduleRepository.LabScheduleData.Success(data.data[week] ?: emptyList())
            }
        }
    }

    override suspend fun cleanLabScheduleCache() {
        logger.i("清除实验课表缓存")
        labScheduleCacheDataSource.cleanLabScheduleCache()
    }
}
