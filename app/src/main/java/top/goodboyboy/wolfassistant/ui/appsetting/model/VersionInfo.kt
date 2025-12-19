package top.goodboyboy.wolfassistant.ui.appsetting.model

data class VersionInfo(
    val version: String,
    val htmlUrl: String,
    val isPrerelease: Boolean,
    val body: String,
)
