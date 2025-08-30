package top.goodboyboy.wolfassistant.ui.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import top.goodboyboy.wolfassistant.common.Event

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewCompose(
    url: String,
    accessToken: String,
    headerTokenKeyName: String,
    urlTokenKeyName: String,
    refreshEvent: Event<Unit>?,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onProgressChanged: (Int) -> Unit,
    onRequestLocation: ((origin: String, callback: GeolocationPermissions.Callback) -> Unit)? = null,
    onError: (WebResourceRequest?, WebResourceError?) -> Unit,
    onActivityNotFoundException: () -> Unit,
    onTitleReceived: (String) -> Unit,
    onWebViewDispose: () -> Unit,
    onNavBack: () -> Unit,
) {
    val context = LocalContext.current
    val webView =
        remember {
            WebView.setWebContentsDebuggingEnabled(false)
            WebView(context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setGeolocationEnabled(true)
                settings.userAgentString += " SuperApp"
                webViewClient =
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?,
                        ): Boolean {
                            if (url != null) {
                                if (isExternalLink(url)) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        view?.context?.startActivity(intent)
                                        return true
                                    } catch (e: ActivityNotFoundException) {
                                        onActivityNotFoundException()
                                    }
                                }
                            }
                            return false
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: android.graphics.Bitmap?,
                        ) {
                            onPageStarted.invoke()
                        }

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?,
                        ) {
                            onPageFinished.invoke()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            super.onReceivedError(view, request, error)
                            onError(request, error)
                        }
//                override fun shouldInterceptRequest(
//                    view: WebView,
//                    request: WebResourceRequest
//                ): WebResourceResponse? {
//                    val ua = request.requestHeaders["User-Agent"]
//                    println("Current UA in request: $ua")
//                    return null
//                }
                    }
                webChromeClient =
                    object : WebChromeClient() {
                        override fun onProgressChanged(
                            view: WebView?,
                            newProgress: Int,
                        ) {
                            super.onProgressChanged(view, newProgress)
                            onProgressChanged(newProgress)
                        }

                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?,
                        ) {
                            if (origin != null && callback != null) {
                                onRequestLocation?.invoke(origin, callback)
                            } else {
                                super.onGeolocationPermissionsShowPrompt(origin, callback)
                            }
                        }

                        override fun onReceivedTitle(
                            view: WebView?,
                            title: String?,
                        ) {
                            onTitleReceived.invoke(title ?: "")
                        }
                    }
            }
        }

    LaunchedEffect(Unit) {
        val uriObj = url.toUri()
        val builder = uriObj.buildUpon()
        if (uriObj.host == "mycas.hut.edu.cn") {
            builder.appendQueryParameter("idToken", accessToken)
        }

        if (urlTokenKeyName != "") {
            builder.appendQueryParameter(urlTokenKeyName, accessToken)
        }
        val newUrl = builder.build().toString()

        val cookieString = "userToken=$accessToken; Path=/"
        CookieManager.getInstance().setCookie(url, cookieString)

        val header =
            mutableMapOf(
                "X-Requested-With" to "com.supwisdom.hut",
            )

        if (headerTokenKeyName != "") {
            header.put(headerTokenKeyName, accessToken)
        }
        webView.loadUrl(newUrl, header)
    }

    LaunchedEffect(refreshEvent) {
        refreshEvent?.getContent()?.let {
            webView.reload()
        }
    }

    DisposableEffect(webView) {
        onDispose {
            onWebViewDispose()
            webView.stopLoading()
            webView.destroy()
        }
    }
    AndroidView(
        factory = { webView },
    )

    BackHandler(
        enabled = true,
    ) {
        webView.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                onNavBack()
            }
        }
    }
}

private fun isExternalLink(url: String): Boolean =
    url.startsWith("weixin") || url.startsWith("bankabc") || url.startsWith("alipays")
