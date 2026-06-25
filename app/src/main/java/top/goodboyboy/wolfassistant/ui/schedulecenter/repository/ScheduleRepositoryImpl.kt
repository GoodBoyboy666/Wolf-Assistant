package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository.ScheduleData
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl
    @Inject
    constructor(
        private val scheduleCacheDataSource: ScheduleCacheDataSource,
        private val scheduleRemoteDataSource: ScheduleRemoteDataSource,
        private val logger: AppLogger,
    ) : ScheduleRepository {
        override suspend fun getSchedule(
            accessToken: String,
            startDate: LocalDate,
            endDate: LocalDate,
            forceRefresh: Boolean,
        ): ScheduleData {
            if (!forceRefresh) {
                logger.i("获取课表: 检查缓存")
                val cache =
                    scheduleCacheDataSource.getSchedule(
                        startDate,
                        endDate,
                    )

                when (cache) {
                    is ScheduleCacheDataSource.DataResult.Error -> {
                        logger.e(cache.error.cause, "缓存异常")
                        return ScheduleData.Failed(cache.error)
                    }

                    ScheduleCacheDataSource.DataResult.NoCache -> {
                        logger.i("获取课表: 缓存未命中，请求远程数据")
                    }
                    is ScheduleCacheDataSource.DataResult.Success -> {
                        logger.i("获取课表: 缓存命中")
                        return ScheduleData.Success(cache.list.flatten())
                    }
                }
            } else {
                logger.i("获取课表: 强制刷新，跳过缓存")
            }

            val remote =
                scheduleRemoteDataSource.getSchedule(
                    accessToken,
                    startDate,
                    endDate,
                )
            when (remote) {
                is ScheduleRemoteDataSource.DataResult.Error -> {
                    logger.e(remote.error.cause, "远程拉取异常")
                    return ScheduleData.Failed(remote.error)
                }

                is ScheduleRemoteDataSource.DataResult.Success -> {
                    logger.i("获取课表: 远程数据获取成功")
                    scheduleCacheDataSource.saveSchedule(
                        startDate,
                        endDate,
                        remote.data,
                    )
                    return ScheduleData.Success(remote.data.flatten())
                }
            }
        }

        override suspend fun cleanScheduleCache() {
            logger.i("清除课表缓存")
            scheduleCacheDataSource.cleanSchedule()
        }
    }
