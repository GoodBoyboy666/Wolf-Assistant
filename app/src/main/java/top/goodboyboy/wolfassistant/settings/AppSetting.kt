package top.goodboyboy.wolfassistant.settings

data class AppSetting(
    val darkMode: Boolean = false,
    val userName: String = "",
    val userID: String = "",
    val userOrganization: String = "",
    val accessToken: String = "",
)
