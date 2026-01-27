package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import kotlinx.coroutines.flow.StateFlow

interface SearchRepository {
    val searchQuery: StateFlow<String>

    suspend fun updateQuery(newQuery: String)
}
