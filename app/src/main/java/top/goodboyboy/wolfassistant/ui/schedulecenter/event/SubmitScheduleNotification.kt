package top.goodboyboy.wolfassistant.ui.schedulecenter.event

import top.goodboyboy.wolfassistant.common.TargetedEvent

data class SubmitScheduleNotification(
    override val targetTag: String

):TargetedEvent
