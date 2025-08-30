package top.goodboyboy.wolfassistant.ui.messagecenter

import android.text.Spanned
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(messageItem: MessageItem) {
    val plainText = stripHtmlTagsAndEntities(messageItem.content)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    Card(
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .padding(5.dp)
                .clickable {
                    showBottomSheet = true
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
        ) {
            Text(
                messageItem.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
            Text(
                messageItem.author,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 5.dp),
            )
            Text(
                plainText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = {
            showBottomSheet = false
        }) {
            val annotatedString =
                remember(messageItem.content) {
                    HtmlCompat
                        .fromHtml(messageItem.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        .toAnnotatedString()
                }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
            ) {
                SelectionContainer {
                    Text(
                        text = annotatedString,
                    )
                }
            }
        }
    }
}

fun Spanned.toAnnotatedString(): AnnotatedString =
    buildAnnotatedString {
        val spanned = this@toAnnotatedString
        append(spanned.toString())
//    spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
//        val start = spanned.getSpanStart(span)
//        val end = spanned.getSpanEnd(span)
//    }
    }

fun stripHtmlTagsAndEntities(html: String): String {
    val combinedRegex = "<[^>]*>|&[a-zA-Z0-9#]*;".toRegex()
    return html.replace(combinedRegex, "")
}
