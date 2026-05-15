package top.goodboyboy.wolfassistant.log

interface AppLogger {
    fun tag(customTag: String): AppLogger

    fun v(
        message: String,
        vararg args: Any,
    )

    fun d(
        message: String,
        vararg args: Any,
    )

    fun i(
        message: String,
        vararg args: Any,
    )

    fun w(
        message: String,
        vararg args: Any,
    )

    fun e(
        throwable: Throwable?,
        message: String,
        vararg args: Any,
    )

    fun json(json: String)
}
