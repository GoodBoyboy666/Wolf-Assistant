package top.goodboyboy.wolfassistant.ui.messagecenter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepository
import javax.inject.Inject

@HiltViewModel
class MessageCenterViewModel
    @Inject
    constructor(
        private val messageRepository: MessageRepository,
        private val settingsRepository: SettingsRepository,
        private val application: Application,
    ) : ViewModel() {
        //    val messageCategory = listOf("公告", "学工系统", "办事大厅")
        val messageCategory = listOf(application.getString(R.string.announcement), application.getString(R.string.xgxt))
        private val messageFlows = mutableMapOf<Int, Flow<PagingData<MessageItem>>>()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun getMessagePagingFlow(category: Int): Flow<PagingData<MessageItem>> {
            val cachedFlow = messageFlows[category]
            if (cachedFlow != null) {
                return cachedFlow
            }

            val newFlow =
                flow {
                    val accessToken = settingsRepository.getAccessTokenDecrypted()
                    if (accessToken.isBlank()) {
                        emit(messageRepository.createErrorFlow(Throwable("accessToken为空或null")))
                    } else {
                        val appidData =
                            messageRepository.getAppID(
                                accessToken,
                            )
                        when (appidData) {
                            is MessageRepository.AppIDData.Failed -> {
                                emit(messageRepository.createErrorFlow(Throwable("获取APPID失败")))
                            }

                            is MessageRepository.AppIDData.Success -> {
                                val appID =
                                    when (category) {
                                        0 -> "online"
                                        1 -> appidData.data[category]
                                        else -> ""
                                    }
                                emit(
                                    messageRepository.getMessages(
                                        accessToken = accessToken,
                                        appID = appID,
                                    ),
                                )
                            }
                        }
                    }
                }.shareIn(viewModelScope, SharingStarted.Lazily, 1).flatMapLatest { it }.cachedIn(viewModelScope)
            messageFlows[category] = newFlow
            return newFlow
        }
    }
