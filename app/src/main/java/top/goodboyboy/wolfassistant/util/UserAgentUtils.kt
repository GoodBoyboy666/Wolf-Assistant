package top.goodboyboy.wolfassistant.util

import kotlin.random.Random

object UserAgentUtils {
    private val OS_LIST =
        listOf(
            "Windows NT 10.0; Win64; x64",
            "Macintosh; Intel Mac OS X 10_15_7",
            "Macintosh; Intel Mac OS X 11_6",
            "Macintosh; Intel Mac OS X 12_6",
            "Macintosh; Intel Mac OS X 13_5",
            "Macintosh; Intel Mac OS X 14_4",
            "X11; Linux x86_64",
        )

    fun getRandomUserAgent(): String {
        val os = OS_LIST.random()
        val isMac = os.startsWith("Macintosh")

        // 0: Chrome, 1: Edge, 2: Firefox, 3: Safari (Mac only)
        val browserType = if (isMac) Random.nextInt(4) else Random.nextInt(3)

        return when (browserType) {
            0 -> generateChrome(os)
            1 -> generateEdge(os)
            2 -> generateFirefox(os)
            3 -> generateSafari(os)
            else -> generateChrome(os)
        }
    }

    private fun generateChrome(os: String): String {
        val major = Random.nextInt(120, 135)
        val build = Random.nextInt(6000, 7000)
        val patch = Random.nextInt(100, 200)
        return "Mozilla/5.0 ($os) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$major.0.$build.$patch Safari/537.36"
    }

    private fun generateEdge(os: String): String {
        val major = Random.nextInt(120, 135)
        val build = Random.nextInt(6000, 7000)
        val patch = Random.nextInt(100, 200)
        val edgeMajor = Random.nextInt(120, 135)
        val edgeBuild = Random.nextInt(2000, 3000)
        val edgePatch = Random.nextInt(50, 100)
        return "Mozilla/5.0 ($os) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$major.0.$build.$patch " +
            "Safari/537.36 Edg/$edgeMajor.0.$edgeBuild.$edgePatch"
    }

    private fun generateFirefox(os: String): String {
        val major = Random.nextInt(115, 135)
        return "Mozilla/5.0 ($os; rv:$major.0) Gecko/20100101 Firefox/$major.0"
    }

    private fun generateSafari(os: String): String {
        val major = Random.nextInt(16, 18)
        val minor = Random.nextInt(1, 6)
        return "Mozilla/5.0 ($os) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/$major.$minor Safari/605.1.15"
    }
}
