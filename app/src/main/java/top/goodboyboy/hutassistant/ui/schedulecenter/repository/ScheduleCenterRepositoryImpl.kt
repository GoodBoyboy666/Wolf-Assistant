package top.goodboyboy.hutassistant.ui.schedulecenter.repository

import android.util.Log
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.hutassistant.ui.schedulecenter.repository.ScheduleCenterRepository.ScheduleData
import java.time.LocalDate

class ScheduleCenterRepositoryImpl(
    private val scheduleCacheDataSource: ScheduleCacheDataSource,
    private val scheduleRemoteDataSource: ScheduleRemoteDataSource,
) : ScheduleCenterRepository {
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
                Log.e(null, "缓存")
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
                Log.e(null, "远程")
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
