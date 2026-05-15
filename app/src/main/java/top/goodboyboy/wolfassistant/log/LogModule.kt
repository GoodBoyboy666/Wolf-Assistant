package top.goodboyboy.wolfassistant.log

import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LogModule {
    @Provides
    @Singleton
    fun provideAppLogger(
        @ApplicationContext context: Context,
    ): AppLogger {
        // 基础配置
        val config =
            LogConfiguration
                .Builder()
                .logLevel(LogLevel.ALL)
                .tag("AppDebug")
                .enableThreadInfo()
                .enableBorder()
                .build()

        // Logcat 打印器
        val androidPrinter = AndroidPrinter(true)

        // 文件打印器 (利用 Hilt 注入的 context 获取缓存目录)
        val logFolder = File(context.cacheDir, "app_logs").absolutePath
        val filePrinter =
            FilePrinter
                .Builder(logFolder)
                .fileNameGenerator(DateFileNameGenerator())
                // 单个日志文件最大 5MB
                .backupStrategy(FileSizeBackupStrategy2(5 * 1024 * 1024, 1))
                // 自动清理超过 7 天（毫秒）的旧日志文件
                .cleanStrategy(FileLastModifiedCleanStrategy(7L * 24 * 60 * 60 * 1000))
                .build()

        // 执行 xLog 全局初始化
        XLog.init(config, androidPrinter, filePrinter)

        return XLogWrapper()
    }
}
