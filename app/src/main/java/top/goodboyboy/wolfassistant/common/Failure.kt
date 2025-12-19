package top.goodboyboy.wolfassistant.common

sealed class Failure(
    val message: String,
    val cause: Throwable? = null,
) {
    class IOError(
        message: String,
        cause: Throwable?,
    ) : Failure(message, cause)

    class UnknownError(
        cause: Throwable?,
    ) : Failure("未分类异常", cause)

    data class ApiError(
        val code: Int,
        val serverMessage: String?,
        val serverCause: Throwable? = null,
    ) : Failure(serverMessage ?: "服务器发生未知错误", serverCause)

    class SecurityException(
        message: String,
        cause: Throwable?,
    ) : Failure(message, cause)

    class JsonParsingError(
        message: String,
        cause: Throwable,
    ) : Failure(message, cause)

    class HTMLParsingError(
        message: String,
    ) : Failure(message, null)

    class CustomError(
        message: String,
    ) : Failure(message, null)

    class DataError(
        message: String,
    ) : Failure(message, null)
}
