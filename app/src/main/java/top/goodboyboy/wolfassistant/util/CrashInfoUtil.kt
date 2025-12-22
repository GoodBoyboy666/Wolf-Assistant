package top.goodboyboy.wolfassistant.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object CrashInfoUtil {
    fun getDeviceAndAppInfo(context: Context): String {
        val sb = StringBuilder()

        // ---------------------------------------------------------
        // App 版本信息
        // ---------------------------------------------------------
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            sb.append("App Version Name: ${pi.versionName}\n")
            sb.append("App Version Code: ${pi.longVersionCode}\n")
        } catch (e: PackageManager.NameNotFoundException) {
            sb.append("App Version: Unknown\n")
        }

        // ---------------------------------------------------------
        // 操作系统信息
        // ---------------------------------------------------------
        sb.append("Android Version: ${Build.VERSION.RELEASE}\n") // 如 "13"
        sb.append("API Level: ${Build.VERSION.SDK_INT}\n") // 如 "33"
        sb.append("Brand: ${Build.BRAND}\n") // 如 "google", "xiaomi"
        sb.append("Manufacturer: ${Build.MANUFACTURER}\n") // 如 "Google"
        sb.append("Model: ${Build.MODEL}\n") // 如 "Pixel 7"

        // ---------------------------------------------------------
        // 硬件/架构信息
        // ---------------------------------------------------------
        sb.append("CPU ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}\n")
        sb.append("Product: ${Build.PRODUCT}\n")
        sb.append("Device: ${Build.DEVICE}\n")

        // ---------------------------------------------------------
        // 屏幕信息
        // ---------------------------------------------------------
        val metrics = context.resources.displayMetrics
        sb.append("Screen Resolution: ${metrics.widthPixels} x ${metrics.heightPixels}\n")
        sb.append("Screen Density: ${metrics.density} (DPI: ${metrics.densityDpi})\n")

        // ---------------------------------------------------------
        // 时间戳
        // ---------------------------------------------------------
        sb.append(
            "Crash Time: ${java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault(),
            ).format(java.util.Date())}\n",
        )

        return sb.toString()
    }
}
