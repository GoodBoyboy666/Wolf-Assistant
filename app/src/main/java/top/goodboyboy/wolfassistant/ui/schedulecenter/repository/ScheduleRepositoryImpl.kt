package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository.ScheduleData
import java.time.LocalDate

class ScheduleRepositoryImpl(
    private val scheduleCacheDataSource: ScheduleCacheDataSource,
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource,
    private val logger: AppLogger,
) : ScheduleRepository {
    override suspend fun getSchedule(
        accessToken: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): ScheduleData {
        val cache =
            scheduleCacheDataSource.getSchedule(
                startDate,
                endDate,
            )

        when (cache) {
            is ScheduleCacheDataSource.DataResult.Error -> {
                logger.e(null, "缓存异常")
                return ScheduleData.Failed(cache.error)
            }

            ScheduleCacheDataSource.DataResult.NoCache -> {}
            is ScheduleCacheDataSource.DataResult.Success -> {
                return ScheduleData.Success(cache.list)
            }
        }

        val remote =
            scheduleRemoteDataSource.getSchedule(
                accessToken,
                startDate,
                endDate,
            )
        when (remote) {
            is ScheduleRemoteDataSource.DataResult.Error -> {
                logger.e(null, "远程拉取异常")
                return ScheduleData.Failed(remote.error)
            }

            is ScheduleRemoteDataSource.DataResult.Success -> {
                scheduleCacheDataSource.saveSchedule(
                    startDate,
                    endDate,
                    remote.data,
                )
                return ScheduleData.Success(remote.data)
            }
        }
    }

    override suspend fun cleanScheduleCache() {
        scheduleCacheDataSource.cleanSchedule()
    }
}
