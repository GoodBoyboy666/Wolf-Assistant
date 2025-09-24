package top.goodboyboy.wolfassistant.ui.appsetting

object GlobalInitConfig {
    var disableSSL: Boolean = false
        private set
    var onlyIPv4: Boolean = false
        private set

    fun setConfig(
        disableSsl: Boolean,
        useOnlyIPv4: Boolean,
    ) {
        this.disableSSL = disableSsl
        this.onlyIPv4 = useOnlyIPv4
    }
}
