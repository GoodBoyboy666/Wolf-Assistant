package top.goodboyboy.wolfassistant.log

import com.elvishew.xlog.XLog

class XLogWrapper(
    private val specificTag: String? = null,
) : AppLogger {
    override fun tag(customTag: String): AppLogger = XLogWrapper(customTag)

    override fun v(
        message: String,
        vararg args: Any,
    ) = specificTag?.let { XLog.tag(it).v(message, *args) } ?: XLog.v(message, *args)

    override fun d(
        message: String,
        vararg args: Any,
    ) = specificTag?.let { XLog.tag(it).d(message, *args) } ?: XLog.d(message, *args)

    override fun i(
        message: String,
        vararg args: Any,
    ) = specificTag?.let { XLog.tag(it).i(message, *args) } ?: XLog.i(message, *args)

    override fun w(
        message: String,
        vararg args: Any,
    ) = specificTag?.let { XLog.tag(it).w(message, *args) } ?: XLog.w(message, *args)

    override fun e(
        throwable: Throwable?,
        message: String,
        vararg args: Any,
    ) = specificTag?.let { XLog.tag(it).e(message, throwable, *args) } ?: XLog.e(message, throwable, *args)

    override fun json(json: String) = specificTag?.let { XLog.tag(it).json(json) } ?: XLog.json(json)
}
