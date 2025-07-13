package top.goodboyboy.hutassistant.settings

data class AppSetting(
    val darkMode: Boolean = false,
    val userName: String = "",
    val userID: String = "",
    val userOrganization: String = "",
    val accessToken: String = "",
)
