package top.goodboyboy.wolfassistant.api.hutapi

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SafeApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnsafeApi
