package az.test.keyboardinapp.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard

@Composable
inline fun WithClipboardText(
    clipboard: Clipboard,
    content: @Composable (String) -> Unit,
) {
    val clipboardText = clipboard
        .getTextOrNull()
        // remove new lines and multiple spaces
        ?.replace("\n", " ")?.replace(Regex("\\s+"), " ")
        // take only first 61 characters if longer than 64
        ?.let { if (it.length > 64) "${it.take(61)}..." else it }
        ?: return

    content(clipboardText)
}

fun Clipboard.getTextOrNull(): String? {
    return if ((nativeClipboard.primaryClip?.itemCount ?: 0) > 0) {
        val item = nativeClipboard.primaryClip?.getItemAt(0)
        item?.text?.toString()?.ifBlank { null }
    } else {
        null
    }
}