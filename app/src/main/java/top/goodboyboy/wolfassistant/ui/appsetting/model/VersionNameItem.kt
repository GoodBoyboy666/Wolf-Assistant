package top.goodboyboy.wolfassistant.ui.appsetting.model

data class VersionNameItem(
    val majorVersionNumber: Int,
    val secondaryVersionNumber: Int,
    val revisionVersionNumber: Int,
    val others: String,
    val versionNameString: String,
)
