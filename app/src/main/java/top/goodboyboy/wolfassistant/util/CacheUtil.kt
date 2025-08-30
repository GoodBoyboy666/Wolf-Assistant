package top.goodboyboy.wolfassistant.util

import android.content.Context
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object CacheUtil {
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.cacheDir)
        context.externalCacheDir?.let {
            cacheSize += getFolderSize(it)
        }
        val webViewCacheDir = File(context.filesDir.parentFile, "app_webview")
        if (webViewCacheDir.exists()) {
            cacheSize += getFolderSize(webViewCacheDir)
        }

        return formatSize(cacheSize)
    }

    fun clearAllCache(context: Context) {
        context.cacheDir.deleteDirectory()
        context.externalCacheDir?.deleteDirectory()
        // 清理 WebView 缓存
        val webViewCacheDir = File(context.filesDir.parentFile, "app_webview")
        if (webViewCacheDir.exists()) {
            webViewCacheDir.deleteDirectory()
        }
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) {
            return 0L
        }
        var size = 0L
        try {
            file.walk().forEach {
                if (it.isFile) {
                    size += it.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble()),
        ) + " " + units[digitGroups]
    }
}
