package top.goodboyboy.wolfassistant.notification

sealed interface NotifyIntent {
    data class ShowScheduleNotification(
        val id: Int,
        val title: String,
        val content: String,
    ) : NotifyIntent
}
