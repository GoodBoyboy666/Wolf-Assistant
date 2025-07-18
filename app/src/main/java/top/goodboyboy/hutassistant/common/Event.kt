package top.goodboyboy.hutassistant.common

class Event<out T>(
    private val content: T,
) {
    private var hasBeenHandled = false

    /**
     * 如果事件还未被处理则返回内容，并标记为已处理。
     * @return 返回事件内容，如果已被处理过则返回 null。
     */
    fun getContent(): T? =
        if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }

    /**
     * 查看事件内容但不消费。
     * @return 返回事件内容无论是否已被处理。
     */
    fun peekContent(): T = content
}
