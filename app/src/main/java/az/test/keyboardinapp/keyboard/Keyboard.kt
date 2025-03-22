package az.test.keyboardinapp.keyboard

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.test.keyboardinapp.R
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DisableSoftKeyboard(
    disable: Boolean = true,
    content: @Composable () -> Unit,
) {
    InterceptPlatformTextInput(
        interceptor = { request, nextHandler ->
            if (!disable) {
                nextHandler.startInputMethod(request)
            } else {
                awaitCancellation()
            }
        },
        content = content,
    )
}

@Composable
fun AnimatedKeyboard(
    isKeyboardVisible: Boolean,
    onKeyClick: (Char) -> Unit,
    onPaste: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    pasteUsageRule: PasteUsageRule = PasteUsageRule.None,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isKeyboardVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        CustomKeyboard(
            pasteUsageRule = pasteUsageRule,
            onKeyClick = onKeyClick,
            onPaste = onPaste,
            onBackspace = onBackspace,
            onClear = onClear,
            onDone = onDone,
            onClose = onClose,
            keyboardType = keyboardType
        )
    }
}

@Composable
fun CustomKeyboard(
    pasteUsageRule: PasteUsageRule,
    onKeyClick: (Char) -> Unit,
    onPaste: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    onClose: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    BackHandler {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp)
    )  {

        PasteRuleValidatedContent(
            rule = pasteUsageRule,
            clipboardManager = LocalClipboardManager.current
        ) { text ->
            ElevatedAssistChip(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = text) },
                onClick = { onPaste(text) },
                trailingIcon = { Icon(painter = painterResource(R.drawable.ic_paste_24), contentDescription = null) },
                colors = AssistChipDefaults.elevatedAssistChipColors().copy(
                    containerColor = Color.LightGray,
                    labelColor = Color.Black,
                    trailingIconContentColor = Color.Black,
                )
            )
        }


        when (keyboardType) {
            KeyboardType.Text -> {
                TextKeyboard(
                    onKeyClick = onKeyClick,
                    onBackspace = onBackspace,
                    onComplete = onDone,
                    onClear = onClear
                )
            }

            KeyboardType.Number, KeyboardType.Decimal, KeyboardType.PhoneNumber -> NumberKeyboard(
                onKeyClick = onKeyClick,
                onBackspace = onBackspace,
                onComplete = onDone,
                onClear = onClear,
                keyboardType = keyboardType
            )
        }
    }

}

@Composable
private fun ColumnScope.TextKeyboard(
    onKeyClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onComplete: () -> Unit,
) {
    var uppercased by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(KeyboardLanguage.EN) }
    var contentKeyType by remember(language) {
        mutableStateOf<ContentKeyType>(ContentKeyType.Letter(language = language))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        KeyboardKeyMap.numberRow.forEach {
            val key = it.value
            CharacterButton(
                char = key,
                onClick = { onKeyClick(key) },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }

    KeyboardKeyMap.getContentRows(contentKeyType).forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { contentKey ->
                when (contentKey) {
                    KeyboardKeys.Shift -> {
                        IconButton(
                            iconId = if (uppercased) R.drawable.ic_shift_active else R.drawable.ic_shift,
                            isRounded = false,
                            onClick = {
                                uppercased = !uppercased
                            },
                            modifier = Modifier
                                .weight(1.5f)
                        )
                    }

                    is KeyboardKeys.Character -> {
                        val key = contentKey.value
                        val sendKey = if (uppercased) key.uppercase().first() else key

                        CharacterButton(
                            char = sendKey,
                            onClick = {
                                onKeyClick(sendKey)
                            },
                            modifier = Modifier
                                .weight(1f)
                        )
                    }

                    KeyboardKeys.Backspace -> {
                        IconButton(
                            iconId = R.drawable.ic_backspace,
                            isRounded = false,
                            onClick = {
                                onBackspace()
                            },
                            onLongClick = {
                                onClear()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                        )
                    }

                    else -> {
                        // not handled here
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        KeyboardKeyMap.utilityRow.forEach {
            when (it) {
                KeyboardKeys.CharacterSwitch -> {
                    val keyText = when (contentKeyType) {
                        is ContentKeyType.Letter -> "?123"
                        is ContentKeyType.SpecialCharacter -> when (language) {
                            KeyboardLanguage.AZ -> "ABC"
                            KeyboardLanguage.EN -> "ABC"
                            KeyboardLanguage.RU -> "АБВ"
                        }
                    }
                    TextButton(
                        text = keyText,
                        isRounded = true,
                        onClick = {
                            contentKeyType = if (contentKeyType is ContentKeyType.Letter) {
                                ContentKeyType.SpecialCharacter
                            } else {
                                ContentKeyType.Letter(language = language)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                KeyboardKeys.LanguageSwitch -> {
                    IconButton(
                        iconId = R.drawable.ic_language,
                        isRounded = true,
                        onClick = {
                            language = when (language) {
                                KeyboardLanguage.AZ -> KeyboardLanguage.EN
                                KeyboardLanguage.EN -> KeyboardLanguage.RU
                                KeyboardLanguage.RU -> KeyboardLanguage.AZ
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                KeyboardKeys.Space -> {
                    KeyButton(
                        content = {
                            Text(
                                text = " ",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        },
                        onClick = {
                            onKeyClick(' ')
                        },
                        modifier = Modifier
                            .weight(4f)
                    )
                }

                KeyboardKeys.Done -> {
                    IconButton(
                        iconId = R.drawable.ic_done_24,
                        isRounded = true,
                        onClick = {
                            onComplete()
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                else -> {
                    // not handled here
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.NumberKeyboard(
    keyboardType: KeyboardType,
    onKeyClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onComplete: () -> Unit,
    onClear: () -> Unit,
) {
    require(
        value = keyboardType != KeyboardType.Text,
        lazyMessage = { "Unsupported keyboard type: $keyboardType" }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..3).forEach {
                    val key = it.toString().first()
                    CharacterButton(
                        char = key,
                        onClick = { onKeyClick(key) },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (4..6).forEach {
                    val key = it.toString().first()
                    CharacterButton(
                        char = key,
                        onClick = { onKeyClick(key) },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (7..9).forEach {
                    val key = it.toString().first()
                    CharacterButton(
                        char = key,
                        onClick = { onKeyClick(key) },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isDecimal = keyboardType == KeyboardType.Decimal
                val isPhoneNumber = keyboardType == KeyboardType.PhoneNumber

                val spaceChar = ' '
                val zeroKey = '0'
                val pointKey = '.'
                val plusKey = '+'

                CharacterButton(
                    char = if (isPhoneNumber) plusKey else spaceChar,
                    onClick = { if (isPhoneNumber) onKeyClick(plusKey) },
                    modifier = Modifier.weight(1f),
                    isEnabled = isPhoneNumber
                )

                CharacterButton(
                    char = zeroKey,
                    onClick = { onKeyClick(zeroKey) },
                    modifier = Modifier.weight(1f)
                )

                CharacterButton(
                    char = if (isDecimal) pointKey else spaceChar,
                    onClick = { if (isDecimal) onKeyClick(pointKey) },
                    modifier = Modifier.weight(1f),
                    isEnabled = isDecimal
                )

            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom)
        ) {
            IconButton(
                iconId = R.drawable.ic_backspace,
                isRounded = true,
                onClick = onBackspace,
                onLongClick = onClear,
                modifier = Modifier
                    .fillMaxWidth()
            )

            IconButton(
                iconId = R.drawable.ic_done_24,
                isRounded = true,
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun CharacterButton(
    char: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    isEnabled: Boolean = true,
) {
    KeyButton(
        content = {
            Text(
                text = char.toString(),
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        isEnabled = isEnabled
    )
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    text: String,
    isRounded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isEnabled: Boolean = true,
) {
    KeyButton(
        content = {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Black
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        isTool = true,
        isRounded = isRounded,
        isEnabled = isEnabled
    )
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    iconId: Int,
    isRounded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isEnabled: Boolean = true,
) {
    KeyButton(
        content = {
            Icon(
                imageVector = ImageVector.vectorResource(id = iconId),
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp),
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        isTool = true,
        isRounded = isRounded,
        isEnabled = isEnabled
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isRounded: Boolean = false,
    isTool: Boolean = false,
    isEnabled: Boolean = true,
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(44.dp)
            .clip(if (isRounded) RoundedCornerShape(22.dp) else RoundedCornerShape(4.dp))
            .combinedClickable(
                enabled = isEnabled,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
                onLongClick = {
                    onLongClick()
                }
            )
            .background(if (isTool) Color.LightGray else Color.White),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun KeyboardDefaultPrev() {

    CustomKeyboard(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        onClose = {},
        keyboardType = KeyboardType.Text,
        pasteUsageRule = PasteUsageRule.None
    )
}

@Preview
@Composable
private fun KeyboardNumberPrev() {

    CustomKeyboard(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        onClose = {},
        keyboardType = KeyboardType.Number,
        pasteUsageRule = PasteUsageRule.None
    )
}

@Preview
@Composable
private fun KeyboardDecimalPrev() {
    CustomKeyboard(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        onClose = {},
        keyboardType = KeyboardType.Decimal,
        pasteUsageRule = PasteUsageRule.None
    )
}

@Preview
@Composable
private fun KeyboardPhonePrev() {
    CustomKeyboard(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        onClose = {},
        keyboardType = KeyboardType.PhoneNumber,
        pasteUsageRule = PasteUsageRule.None
    )
}
