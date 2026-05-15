package top.goodboyboy.wolfassistant.ui.schedulecenter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import top.goodboyboy.wolfassistant.util.AppPermissionChecker

@Composable
fun SubmitScheduleNotificationDialog(
    onAllPermissionAllowed: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current



    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // 用户从设置页返回后，再次检查是否给了权限
        if (AppPermissionChecker.hasExactAlarmPermission(context)) {
            onAllPermissionAllowed()
        } else {
//            Toast.makeText(context, "必须开启闹钟权限才能准时提醒哦！", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 通知权限拿到手了，接着去检查/请求闹钟权限
            checkAndRequestExactAlarm(context, exactAlarmLauncher, onAllPermissionAllowed)
        } else {
//            Toast.makeText(context, "没有通知权限，您将无法收到提醒！", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Info, contentDescription = "提示")
        },
        title = {
            Text(text = "订阅本周课表")
        },
        text = {
            Text(text = "为了能够准时提醒您即将开始的课程，请允许应用发送通知和设置闹钟权限。")
        },
        onDismissRequest = {
            onCancel()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    startPermissionCheckFlow(
                        context = context,
                        notificationLauncher = notificationLauncher,
                        exactAlarmLauncher = exactAlarmLauncher,
                        onSuccess = onAllPermissionAllowed
                    )
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCancel()
                }
            ) {
                Text("取消")
            }
        }
    )
}


private fun startPermissionCheckFlow(
    context: Context,
    notificationLauncher: ManagedActivityResultLauncher<String, Boolean>,
    exactAlarmLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onSuccess: () -> Unit
) {
    if (!AppPermissionChecker.hasNotificationPermission(context)) {
        // 去申请通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        // 通知权限已就绪，检查闹钟权限
        checkAndRequestExactAlarm(context, exactAlarmLauncher, onSuccess)
    }
}

private fun checkAndRequestExactAlarm(
    context: Context,
    exactAlarmLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onSuccess: () -> Unit
) {
    if (!AppPermissionChecker.hasExactAlarmPermission(context)) {
        // 去申请精确闹钟权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${context.packageName}".toUri()
            }
            exactAlarmLauncher.launch(intent)
        }
    } else {
        onSuccess()
    }
}
