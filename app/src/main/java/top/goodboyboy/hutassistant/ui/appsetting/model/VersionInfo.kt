package top.goodboyboy.hutassistant.ui.appsetting.model

data class VersionInfo(
    val versionNameItem: VersionNameItem,
    val htmlUrl: String,
    val isPrerelease: Boolean,
    val body: String,
)
