package top.goodboyboy.wolfassistant.task.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.common.AlarmTriggeredEvent
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.log.AppLogger
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var globalEventBus: GlobalEventBus

    @Inject
    lateinit var logger: AppLogger
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val bizTypeName = intent.getStringExtra("BIZ_TYPE") ?: return
        val eventId = intent.getLongExtra("EVENT_ID", -1L)
        if (eventId == -1L) return
        val bizType = runCatching { AlarmBizType.valueOf(bizTypeName) }.getOrDefault(AlarmBizType.UNKNOWN)
        val pendingResult = goAsync()
        logger.tag("AlarmReceiver").d("Received alarm: bizType=$bizTypeName, eventId=$eventId")
        scope.launch {
            try {
                globalEventBus.emit(AlarmTriggeredEvent(bizTypeName, bizType, eventId))
            } finally {
                pendingResult.finish()
            }
        }
    }
}
