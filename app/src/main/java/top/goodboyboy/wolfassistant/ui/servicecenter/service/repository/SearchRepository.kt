package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import kotlinx.coroutines.flow.StateFlow

interface SearchRepository {
    val searchQuery: StateFlow<String>

    fun updateQuery(newQuery: String)
}
