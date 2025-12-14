package top.goodboyboy.wolfassistant.util

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RsaPemUtils {
    // 指定填充模式：配合服务端常见的 PKCS#1 v1.5
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    /**
     * 将 PEM 格式的字符串转换为 PublicKey 对象
     */
    fun getPublicKeyFromPem(pem: String): PublicKey {
        try {
            // 1. 强力清洗步骤
            val publicKeyPEM =
                pem
                    // 使用正则删除头部：匹配任意数量的横杠 + BEGIN PUBLIC KEY + 任意数量横杠
                    .replace(Regex("[-]{2,}(BEGIN|END) PUBLIC KEY[-]{2,}"), "")
                    // 使用正则删除所有空白字符（包括空格、换行、Tab、全角空格等）
                    .replace(Regex("\\s+"), "")

            // 2. 检查清洗结果是否为空
            if (publicKeyPEM.isEmpty()) {
                throw IllegalArgumentException("清洗后的公钥为空，请检查输入")
            }

            // 3. Base64 解码
            val encodedBytes = Base64.decode(publicKeyPEM, Base64.NO_WRAP)

            // 4. 生成公钥
            val keySpec = X509EncodedKeySpec(encodedBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePublic(keySpec)
        } catch (e: IllegalArgumentException) {
            // 专门捕获 Base64 错误
            throw IllegalArgumentException("公钥格式错误(Base64): 请检查是否包含非法字符", e)
        } catch (e: Exception) {
            // 其他错误 (如 KeySpec 错误)
            throw IllegalArgumentException("公钥解析失败: ${e.message}", e)
        }
    }

    /**
     * 加密字符串
     * @param data 要加密的原始字符串
     * @param publicKey 从 getPublicKeyFromPem 获取的对象
     * @return 加密后的 Base64 字符串
     */
    fun encrypt(
        data: String,
        publicKey: PublicKey,
    ): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        // 执行加密
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // 返回 Base64 字符串
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
}
