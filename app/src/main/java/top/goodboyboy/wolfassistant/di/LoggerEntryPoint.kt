package top.goodboyboy.wolfassistant.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.log.AppLogger

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LoggerEntryPoint {
    fun logger(): AppLogger
}
