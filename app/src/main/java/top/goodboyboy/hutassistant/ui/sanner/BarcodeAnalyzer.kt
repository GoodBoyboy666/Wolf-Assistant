package top.goodboyboy.hutassistant.ui.sanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.net.toUri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    // 将ML Kit更换为ZXing
    //    private val scanner = BarcodeScanning.getClient()
    private val reader =
        MultiFormatReader().apply {
            // 配置解码器，指定可能的条码格式，可以提高解码速度和准确性
            val hints =
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to
                        listOf(
                            BarcodeFormat.QR_CODE,
                            BarcodeFormat.CODE_128,
                            BarcodeFormat.EAN_13,
                        ),
                )
            setHints(hints)
        }

    @Volatile
    private var isDetected = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (isDetected) {
            imageProxy.close()
            return
        }
//        val mediaImage = imageProxy.image ?: run {
//            imageProxy.close()
//            return
//        }

        val yBuffer = imageProxy.planes[0].buffer.toReadOnlyBuffer()
        val yByteArray = ByteArray(yBuffer.remaining())
        yBuffer.get(yByteArray)

        val source =
            PlanarYUVLuminanceSource(
                yByteArray,
                imageProxy.width,
                imageProxy.height,
                0,
                0,
                imageProxy.width,
                imageProxy.height,
                false,
            )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(binaryBitmap)
            val rawValue = result.text
//            Log.d(null, rawValue)
            if (checkUrl(rawValue)) {
                isDetected = true
                onBarcodeScanned(rawValue)
            }
        } catch (e: NotFoundException) {
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
            reader.reset()
        }

//        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//        scanner.process(image)
//            .addOnSuccessListener { barcodes ->
//                for (barcode in barcodes) {
//                    barcode.rawValue?.let { rawValue ->
//                        if (checkUrl(rawValue)) {
//                            isDetected = true
//                            onBarcodeScanned(rawValue)
//                            break
//                        }
//                    }
//                }
//            }
//            .addOnCompleteListener {
//                imageProxy.close()
//            }
    }

    private fun ByteBuffer.toReadOnlyBuffer(): ByteBuffer = this.asReadOnlyBuffer()

    fun checkUrl(url: String): Boolean {
        try {
            val uri = url.toUri()
            val scheme = uri.scheme
            val host = uri.host
            return (scheme == "https" || scheme == "http") && host == "mycas.hut.edu.cn"
        } catch (e: Exception) {
            return false
        }
    }
}
