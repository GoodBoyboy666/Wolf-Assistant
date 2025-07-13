package top.goodboyboy.hutassistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen route
 *
 * @property route 路由名称
 * @property title 标题
 * @property icon 路由图标
 * @constructor Create empty Screen route
 */
sealed class ScreenRoute(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    object Home : ScreenRoute("home", "首页", Icons.Rounded.Home)

    object ServiceCenter : ScreenRoute("service_center", "服务中心", Icons.Rounded.Apps)

    object MessageCenter : ScreenRoute("message_center", "消息中心", Icons.Rounded.MarkEmailUnread)

    object Schedule : ScreenRoute("schedule", "课表", Icons.Rounded.CalendarMonth)

    object PersonalCenter : ScreenRoute("personal_center", "个人中心", Icons.Rounded.Person)

    companion object {
        val items by lazy {
            listOf(Home, ServiceCenter, MessageCenter, Schedule, PersonalCenter)
        }
    }
}
