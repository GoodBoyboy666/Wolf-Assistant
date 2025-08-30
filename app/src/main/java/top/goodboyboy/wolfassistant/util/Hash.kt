package top.goodboyboy.wolfassistant.util

import java.security.MessageDigest

object Hash {
    fun sha256(input: String): String {
        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(input.toByteArray())
        return bytes.toHex()
    }

    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
