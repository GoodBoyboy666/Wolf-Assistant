package top.goodboyboy.wolfassistant.common

import java.util.UUID

interface AppEvent {
    val id: String
        get() = UUID.randomUUID().toString()

    val timestamp: Long
        get() = System.currentTimeMillis()
}

interface TargetedEvent : AppEvent {
    val targetTag: String // 接收者的标签
}

@Suppress("unused")
interface BroadcastEvent : AppEvent
