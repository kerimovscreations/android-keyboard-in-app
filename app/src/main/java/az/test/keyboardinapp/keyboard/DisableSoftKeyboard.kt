package az.test.keyboardinapp.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DisableSoftKeyboard(
    content: @Composable () -> Unit,
) {
    InterceptPlatformTextInput(
        interceptor = { _, _ ->
            awaitCancellation()
        },
        content = content,
    )
}