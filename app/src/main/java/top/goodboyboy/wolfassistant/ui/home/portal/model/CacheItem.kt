package top.goodboyboy.wolfassistant.ui.home.portal.model

import java.time.LocalDateTime

data class CacheItem<T>(
    val createTime: LocalDateTime,
    val cacheObject: T,
)
