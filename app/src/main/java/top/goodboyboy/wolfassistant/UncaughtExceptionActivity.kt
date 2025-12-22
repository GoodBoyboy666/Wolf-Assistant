package top.goodboyboy.wolfassistant

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.goodboyboy.wolfassistant.ui.theme.WolfAssistantTheme
import java.io.File
import kotlin.system.exitProcess

class UncaughtExceptionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        var errorMsg = "读取错误信息失败"
        val filePath = intent.getStringExtra("crash_file_path")
        if (filePath != null) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    errorMsg = file.readText()
                    file.delete()
                }
            } catch (e: Exception) {
                errorMsg = "读取崩溃日志文件出错: ${e.message}"
            }
        }
        setContent {
            WolfAssistantTheme {
                CrashScreen(errorMsg = errorMsg, onRestart = {
                    restartApp(this)
                })
            }
        }
    }

    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        exitProcess(0)
    }
}

@Composable
fun CrashScreen(
    errorMsg: String,
    onRestart: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "⚠️ 程序发生了崩溃",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "请将以下信息长截图反馈至开发者以便修复问题，谢谢！（不包含个人信息）\n\n" +
                            "如果您不希望透露您的设备基础信息，可以仅截图 STACK TRACE 部分。\n\n" +
                            "如果您希望继续使用应用，请点击下方按钮重新启动应用。",
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectionContainer(
                    modifier =
                        Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = errorMsg,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text("重新启动应用", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
