package top.goodboyboy.wolfassistant.common

import top.goodboyboy.wolfassistant.task.alarm.AlarmBizType

data class AlarmTriggeredEvent(
    override val targetTag: String, // 用于路由到具体的业务模块
    val bizType: AlarmBizType, // 业务类型，例如日程提醒、任务提醒等
    val eventId: Long, // 具体的业务ID
) : TargetedEvent
