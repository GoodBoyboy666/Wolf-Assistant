package top.goodboyboy.wolfassistant.ui.schedulecenter.event

import top.goodboyboy.wolfassistant.common.TargetedEvent

data class RollBackToCurrentDateEvent(
    override val targetTag: String,
) : TargetedEvent
