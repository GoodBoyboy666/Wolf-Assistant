package top.goodboyboy.wolfassistant.util

import java.io.File

fun File.deleteDirectory(): Boolean {
    if (!this.exists()) return true
    if (this.isFile) return this.delete()
    val contents = this.listFiles()
    if (contents != null) {
        for (file in contents) {
            if (!file.deleteDirectory()) {
                return false
            }
        }
    }
    return this.delete()
}
