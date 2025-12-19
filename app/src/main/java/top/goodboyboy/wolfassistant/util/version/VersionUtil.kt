package top.goodboyboy.wolfassistant.util.version

object VersionUtil {
    fun parse(versionString: String?): Version {
        if (versionString.isNullOrBlank()) return Version()

        // 预处理
        val cleanString = versionString.trim().removePrefix("v").removePrefix("V")

        // 分离核心与后缀
        val parts = cleanString.split("-", limit = 2)
        val corePart = parts[0]
        val suffixPart = if (parts.size > 1) parts[1] else ""

        // 解析核心数字
        val coreNumbers = corePart.split(".").map { it.toIntOrNull() ?: 0 }
        val major = coreNumbers.getOrElse(0) { 0 }
        val minor = coreNumbers.getOrElse(1) { 0 }
        val patch = coreNumbers.getOrElse(2) { 0 }

        // 解析状态与状态版本
        val (status, statusVer) = parseSuffix(suffixPart)

        return Version(
            major = major,
            minor = minor,
            patch = patch,
            status = status,
            statusVersion = statusVer,
            originalString = versionString,
        )
    }

    private fun parseSuffix(suffix: String): Pair<Version.ReleaseStatus, Int> {
        // 如果没有后缀，说明是正式版 (Stable)
        if (suffix.isEmpty()) return Version.ReleaseStatus.STABLE to 0

        val lowerSuffix = suffix.lowercase()

        // 识别状态
        val status =
            when {
                lowerSuffix.contains("rc") -> Version.ReleaseStatus.RC
                lowerSuffix.contains("beta") -> Version.ReleaseStatus.BETA
                lowerSuffix.contains("alpha") -> Version.ReleaseStatus.ALPHA
                else -> Version.ReleaseStatus.UNKNOWN
            }

        // 提取状态版本号 (beta.2 -> 2)
        val lastPart = lowerSuffix.split(".").lastOrNull()
        val number = lastPart?.toIntOrNull() ?: 0

        return status to number
    }
}
