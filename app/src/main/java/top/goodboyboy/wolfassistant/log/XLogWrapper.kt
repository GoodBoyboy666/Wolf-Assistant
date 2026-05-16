package top.goodboyboy.wolfassistant.log

import com.elvishew.xlog.Logger
import com.elvishew.xlog.printer.Printer

class XLogWrapper(
    private val consolePrinter: Printer,
    private val filePrinter: Printer,
    private val specificTag: String? = null,
) : AppLogger {
    override fun tag(customTag: String): AppLogger = XLogWrapper(consolePrinter, filePrinter, customTag)

    private inline fun executeLog(action: (Logger) -> Unit) {
        val tagToUse = specificTag ?: "AppDebug"

        // 控制台打印 (带边框)
        val consoleLogger =
            Logger
                .Builder()
                .tag(tagToUse)
                .enableBorder() // 开启边框
                .enableThreadInfo() // 打印线程
//            .enableStackTrace(2) // 打印堆栈
                .printers(consolePrinter)
                .build()
        action(consoleLogger)

        // 文件打印 (纯文本)
        val fileLogger =
            Logger
                .Builder()
                .tag(tagToUse)
                .disableBorder() // 关闭边框
                .enableThreadInfo() // 打印线程
                .disableStackTrace() // 关闭堆栈
                .printers(filePrinter)
                .build()
        action(fileLogger)
    }

    override fun v(
        message: String,
        vararg args: Any,
    ) = executeLog { it.v(message, *args) }

    override fun d(
        message: String,
        vararg args: Any,
    ) = executeLog { it.d(message, *args) }

    override fun i(
        message: String,
        vararg args: Any,
    ) = executeLog { it.i(message, *args) }

    override fun w(
        message: String,
        vararg args: Any,
    ) = executeLog { it.w(message, *args) }

    override fun e(
        throwable: Throwable?,
        message: String,
        vararg args: Any,
    ) = executeLog { it.e(message, throwable, *args) }

    override fun json(json: String) = executeLog { it.json(json) }
}
