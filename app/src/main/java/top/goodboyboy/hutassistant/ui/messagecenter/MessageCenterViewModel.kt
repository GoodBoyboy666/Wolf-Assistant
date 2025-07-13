package top.goodboyboy.hutassistant.ui.messagecenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import top.goodboyboy.hutassistant.settings.SettingsRepository
import top.goodboyboy.hutassistant.ui.messagecenter.model.MessageItem
import top.goodboyboy.hutassistant.ui.messagecenter.repository.MessageRepository
import javax.inject.Inject

@HiltViewModel
class MessageCenterViewModel
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        //    val messageCategory = listOf("工大公告", "学工系统", "办事大厅")
        val messageCategory = listOf("工大公告", "学工系统")
        private val messageFlows = mutableMapOf<Int, Flow<PagingData<MessageItem>>>()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun getMessagePagingFlow(category: Int): Flow<PagingData<MessageItem>> {
            val cachedFlow = messageFlows[category]
            if (cachedFlow != null) {
                return cachedFlow
            }

            val newFlow =
                settingsRepository.accessTokenFlow.flatMapLatest { accessToken ->
                    if (accessToken.isNullOrBlank()) {
                        messageRepository.createErrorFlow(Throwable("accessToken为空或null"))
                    } else {
                        val appidData = messageRepository.getAppID(accessToken)
                        when (appidData) {
                            is MessageRepository.AppIDData.Failed -> {
                                messageRepository.createErrorFlow(Throwable("获取APPID失败"))
                            }

                            is MessageRepository.AppIDData.Success -> {
                                val appID =
                                    when (category) {
                                        0 -> "online"
                                        1 -> appidData.data[category]
                                        else -> ""
                                    }
                                messageRepository.getMessages(
                                    accessToken = accessToken,
                                    appID = appID,
                                )
                            }
                        }
                    }.cachedIn(viewModelScope)
                }
            messageFlows[category] = newFlow
            return newFlow
        }
    }
