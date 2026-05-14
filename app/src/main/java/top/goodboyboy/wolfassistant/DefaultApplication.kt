package top.goodboyboy.wolfassistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import top.goodboyboy.wolfassistant.ui.schedulecenter.ScheduleNotificationController
import javax.inject.Inject

@HiltAndroidApp
class DefaultApplication : Application(){
    @Inject
    lateinit var scheduleNotificationController: ScheduleNotificationController
}
