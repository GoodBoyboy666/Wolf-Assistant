package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl
    @Inject
    constructor() : SearchRepository {
        private val _searchQuery = MutableStateFlow("")
        override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        override suspend fun updateQuery(newQuery: String) {
            _searchQuery.value = newQuery
        }
    }
