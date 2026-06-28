package top.goodboyboy.wolfassistant.ui.webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import kotlinx.coroutines.flow.Flow
import top.goodboyboy.wolfassistant.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewCompose(
    url: String,
    accessToken: String,
    headerTokenKeyName: String,
    urlTokenKeyName: String,
    refreshEvent: Flow<Unit>,
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

    // --- File Chooser 状态 ---
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFileChooserDialog by remember { mutableStateOf(false) }

    // 相机拍照 launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraImageUri?.let { filePathCallback?.onReceiveValue(arrayOf(it)) }
                ?: filePathCallback?.onReceiveValue(null)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
        cameraImageUri = null
    }

    // 单文件选择 launcher
    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            filePathCallback?.onReceiveValue(arrayOf(uri))
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    // 多文件选择 launcher
    val multipleFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            filePathCallback?.onReceiveValue(uris.toTypedArray())
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    // CAMERA 权限请求 launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraImageUri?.let { cameraLauncher.launch(it) }
        } else {
            fileChooserLauncher.launch("image/*")
        }
    }

    val webView =
        remember {
            WebView.setWebContentsDebuggingEnabled(false)
            WebView(context.applicationContext).apply {
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
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                        return true
                                    } catch (_: ActivityNotFoundException) {
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

                        override fun onShowFileChooser(
                            webView: WebView?,
                            callback: ValueCallback<Array<Uri>>?,
                            params: FileChooserParams?,
                        ): Boolean {
                            if (callback == null) return false
                            filePathCallback = callback

                            val acceptTypes = params?.acceptTypes ?: arrayOf("*/*")
                            val isCaptureEnabled = params?.isCaptureEnabled ?: false
                            val isImageRequest = acceptTypes.any { it.startsWith("image/") }
                            val isMultiple = params?.mode ==
                                WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE

                            if (isCaptureEnabled || isImageRequest) {
                                showFileChooserDialog = true
                            } else if (isMultiple) {
                                multipleFileLauncher.launch(normalizeMimeType(acceptTypes))
                            } else {
                                fileChooserLauncher.launch(normalizeMimeType(acceptTypes))
                            }
                            return true
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
            header[headerTokenKeyName] = accessToken
        }
        webView.loadUrl(newUrl, header)
    }

    LaunchedEffect(Unit) {
        refreshEvent.collect {
            webView.reload()
        }
    }

    DisposableEffect(webView) {
        onDispose {
            onWebViewDispose()
            webView.apply {
                stopLoading()
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                (parent as? ViewGroup)?.removeView(this)
                clearHistory()
//                clearCache(true)
                destroy()
            }
        }
    }

    AndroidView(factory = { webView })

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

    // 文件选择对话框
    if (showFileChooserDialog) {
        AlertDialog(
            onDismissRequest = {
                showFileChooserDialog = false
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
            },
            title = { Text(stringResource(R.string.choose_file)) },
            confirmButton = {
                TextButton(onClick = {
                    showFileChooserDialog = false
                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        cameraImageUri = createCameraImageUri(context)
                        cameraImageUri?.let { cameraLauncher.launch(it) }
                    } else {
                        cameraImageUri = createCameraImageUri(context)
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text(stringResource(R.string.take_photo))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFileChooserDialog = false
                    fileChooserLauncher.launch("image/*")
                }) {
                    Text(stringResource(R.string.choose_from_gallery))
                }
            },
        )
    }
}

private fun createCameraImageUri(context: android.content.Context): Uri {
    val imageFile = File.createTempFile(
        "webview_camera_", ".jpg", context.cacheDir,
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )
}

private fun normalizeMimeType(acceptTypes: Array<String>): String {
    val mimeMap = MimeTypeMap.getSingleton()
    for (type in acceptTypes) {
        if (type == "*/*" || type.contains("/") && !type.startsWith(".")) {
            return type
        }
        val ext = type.trimStart('.')
        val mime = mimeMap.getMimeTypeFromExtension(ext)
        if (mime != null) return mime
    }
    return "*/*"
}

private fun isExternalLink(url: String): Boolean =
    url.startsWith("weixin") || url.startsWith("bankabc") || url.startsWith("alipays")
