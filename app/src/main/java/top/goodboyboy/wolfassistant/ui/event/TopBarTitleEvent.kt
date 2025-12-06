package top.goodboyboy.wolfassistant.ui.event

import top.goodboyboy.wolfassistant.common.TargetedEvent

data class TopBarTitleEvent(
    override val targetTag: String,
    val title: String,
) : TargetedEvent
