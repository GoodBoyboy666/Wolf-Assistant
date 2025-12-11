package top.goodboyboy.wolfassistant.ui.messagecenter

import androidx.paging.PagingData
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem

/**
 * 测试辅助：创建 PagingData 的简单工厂
 */
object TestPagingDataFactory {
    fun create(vararg items: MessageItem): PagingData<MessageItem> = PagingData.from(items.toList())
}
