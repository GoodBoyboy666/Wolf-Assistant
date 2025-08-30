package top.goodboyboy.wolfassistant.ui.servicecenter.service.model

data class ServiceItem(
    val imageUrl: String,
    val text: String,
    val serviceUrl: String,
    val tokenAccept: TokenKeyName?,
)
