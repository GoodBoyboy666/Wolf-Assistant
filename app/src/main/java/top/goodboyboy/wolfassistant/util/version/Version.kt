package top.goodboyboy.wolfassistant.util.version

data class Version(
    val major: Int = 0,
    val minor: Int = 0,
    val patch: Int = 0,
    val status: ReleaseStatus = ReleaseStatus.STABLE,
    val statusVersion: Int = 0,
    val originalString: String = "",
) : Comparable<Version> {
    enum class ReleaseStatus(
        val weight: Int,
    ) {
        UNKNOWN(0),
        ALPHA(1),
        BETA(2),
        RC(3),
        STABLE(999),
    }

    override fun compareTo(other: Version): Int {
        // 比较核心版本 (Major.Minor.Patch)
        if (this.major != other.major) return this.major - other.major
        if (this.minor != other.minor) return this.minor - other.minor
        if (this.patch != other.patch) return this.patch - other.patch

        // 比较发布状态 (Alpha < Beta < RC < Stable)
        if (this.status != other.status) {
            return this.status.weight - other.status.weight
        }

        // 比较状态版本号 (beta.1 < beta.2)
        return this.statusVersion - other.statusVersion
    }
}
