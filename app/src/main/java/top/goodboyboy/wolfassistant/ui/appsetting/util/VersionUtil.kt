package top.goodboyboy.wolfassistant.ui.appsetting.util

import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionNameItem

object VersionUtil {
    /**
     * 将版本字符串转化为数据类
     * 支持x.x.x-xxx格式转换
     *
     * @param string 版本字符串
     * @return VersionNameItem数据类
     */
    fun getVersionNameItem(string: String): VersionNameItem {
        val others = string.substringAfterLast("-", "")
        val splitVersion = string.split(".")
        val majorVersionNumber = splitVersion[0]
        val secondaryVersionNumber = splitVersion[1]
        val revisionVersionNumber =
            if (others.isEmpty()) {
                splitVersion[2]
            } else {
                splitVersion[2].split('-')[0]
            }
        return VersionNameItem(
            majorVersionNumber = majorVersionNumber.toInt(),
            secondaryVersionNumber = secondaryVersionNumber.toInt(),
            revisionVersionNumber = revisionVersionNumber.toInt(),
            others = others,
            versionNameString = string,
        )
    }
}
