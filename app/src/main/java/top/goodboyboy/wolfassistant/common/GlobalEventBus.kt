package top.goodboyboy.wolfassistant.common

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import top.goodboyboy.wolfassistant.BuildConfig
import javax.inject.Singleton

@Singleton
class GlobalEventBus {
    // extraBufferCapacity = 1: 确保发送不会因为没有订阅者挂起
    // replay = 0: 新来的订阅者不需要收到以前发生的旧事件
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1, replay = 0)
    val events = _events.asSharedFlow()

    // 发送事件
    suspend fun emit(event: AppEvent) {
        printEventLog(event)
        _events.emit(event)
    }

    // 监听特定类型的事件
    inline fun <reified T : TargetedEvent> subscribeToTarget(tag: String): Flow<T> {
        return events
            .filterIsInstance<T>() // 过滤类型
            .filter { it.targetTag == tag } // 过滤标签
    }

    private fun printEventLog(event: AppEvent) {
        if (!BuildConfig.DEBUG) return

        val eventName = event::class.java.simpleName

        val timeString =
            java.text
                .SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
                .format(event.timestamp)
        val metaInfo = "[$timeString | ID:${event.id}]"

        val extraInfo =
            if (event is TargetedEvent) {
                " -> [Target: ${event.targetTag}]"
            } else {
                " -> [Broadcast]"
            }

        Log.d("EventBus", "$metaInfo $eventName$extraInfo : $event")
    }
}
