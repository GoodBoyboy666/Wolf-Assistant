package top.goodboyboy.wolfassistant.ui.event

import top.goodboyboy.wolfassistant.common.TargetedEvent

data class BrowserMenuClickEvent(
    override val targetTag: String,
) : TargetedEvent
