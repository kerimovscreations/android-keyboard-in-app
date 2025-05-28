package az.test.keyboardinapp.keyboard

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.test.keyboardinapp.R

@Composable
fun InAppKeyboard(
    modifier: Modifier = Modifier,
    visible: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
    onClose: () -> Unit,
    clipboard: Clipboard? = null
) {
    // Ensure the keyboard is dismissed when the back button is pressed
    BackHandler {
        onClose()
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        KeyboardLayout(
            keyboardType = keyboardType,
            clipboard = clipboard,
            onKeyClick = { keyChar ->
                // If there's selected text, replace it with the new character
                if (textFieldValue.selection.start != textFieldValue.selection.end) {
                    val textBeforeSelection =
                        textFieldValue.text.substring(0, textFieldValue.selection.start)
                    val textAfterSelection =
                        textFieldValue.text.substring(textFieldValue.selection.end)

                    onTextFieldValueChange(
                        TextFieldValue(
                            textBeforeSelection + keyChar + textAfterSelection,
                            TextRange(textFieldValue.selection.start + 1)
                        )
                    )
                } else {
                    // Insert at cursor position
                    val cursorPos = textFieldValue.selection.start
                    val newText = textFieldValue.text.substring(0, cursorPos) + keyChar +
                            textFieldValue.text.substring(cursorPos)
                    onTextFieldValueChange(TextFieldValue(newText, TextRange(cursorPos + 1)))
                }
            },
            onPaste = {
                val clipboardText = clipboard?.getTextOrNull() ?: return@KeyboardLayout
                onTextFieldValueChange(
                    TextFieldValue(
                        clipboardText,
                        TextRange(clipboardText.length)
                    )
                )
            },
            onBackspace = {
                if (textFieldValue.text.isNotEmpty()) {
                    // If there's a selection, delete the selected text
                    if (textFieldValue.selection.start != textFieldValue.selection.end) {
                        val start =
                            minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                        val end =
                            maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                        if (start >= 0 && end <= textFieldValue.text.length) {
                            val newText = textFieldValue.text.removeRange(start, end)
                            onTextFieldValueChange(TextFieldValue(newText, TextRange(start)))
                        }
                    }
                    // Otherwise delete the character before the cursor
                    else if (textFieldValue.selection.end > 0) {
                        val newText =
                            textFieldValue.text.substring(0, textFieldValue.selection.end - 1) +
                                    textFieldValue.text.substring(textFieldValue.selection.end)
                        onTextFieldValueChange(
                            TextFieldValue(
                                newText,
                                TextRange(textFieldValue.selection.end - 1)
                            )
                        )
                    }
                }
            },
            onClear = {
                if (textFieldValue.text.isNotEmpty()) {
                    onTextFieldValueChange(TextFieldValue("", TextRange(0)))
                }
            },
            onDone = onDone
        )
    }
}

@Composable
private fun KeyboardLayout(
    onKeyClick: (Char) -> Unit,
    onPaste: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    clipboard: Clipboard? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 20.dp)
    ) {
        clipboard?.let {
            WithClipboardText(
                clipboard = it,
            ) { text ->
                ElevatedAssistChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = text,
                            modifier =
                                Modifier
                                    .padding(top = 2.dp, bottom = 2.dp)
                        )
                    },
                    onClick = { onPaste() },
                    trailingIcon = {
                        Box(
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_paste_24),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = AssistChipDefaults.elevatedAssistChipColors().copy(
                        containerColor = Color.LightGray,
                        labelColor = Color.Black,
                        trailingIconContentColor = Color.Black,
                    )
                )
            }
        }

        when (keyboardType) {
            KeyboardType.Text -> {
                TextKeyboard(
                    onKeyClick = onKeyClick,
                    onBackspace = onBackspace,
                    onClear = onClear,
                    onComplete = onDone
                )
            }

            is KeyboardType.Numeric ->
                NumberKeyboard(
                    type = keyboardType.type,
                    onKeyClick = onKeyClick,
                    onBackspace = onBackspace,
                    onComplete = onDone,
                    onClear = onClear
                )
        }
    }
}

@Composable
private fun TextKeyboard(
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
private fun NumberKeyboard(
    type: NumericKeyboardType,
    onKeyClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onComplete: () -> Unit,
    onClear: () -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(3f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..3).forEach {
                    val key = it.toString().first()
                    NumberButton(
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
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (4..6).forEach {
                    val key = it.toString().first()
                    NumberButton(
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
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (7..9).forEach {
                    val key = it.toString().first()
                    NumberButton(
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
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isDecimal = type == NumericKeyboardType.Decimal
                val isPhoneNumber = type == NumericKeyboardType.PhoneNumber

                val zeroKey = '0'
                val pointKey = '.'
                val plusKey = '+'

                NumberButton(
                    char = plusKey,
                    onClick = { if (isPhoneNumber) onKeyClick(plusKey) },
                    modifier = Modifier.weight(1f),
                    isEnabled = true
                )

                NumberButton(
                    char = zeroKey,
                    onClick = { onKeyClick(zeroKey) },
                    modifier = Modifier.weight(1f)
                )

                NumberButton(
                    char = pointKey,
                    onClick = { if (isDecimal) onKeyClick(pointKey) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
        ) {
            IconButton(
                iconId = R.drawable.ic_backspace,
                isRounded = true,
                onClick = onBackspace,
                onLongClick = onClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            IconButton(
                iconId = R.drawable.ic_done_24,
                isRounded = true,
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
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
fun NumberButton(
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
                fontSize = 22.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp)),
        isEnabled = isEnabled,
        isRounded = true
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
        modifier = Modifier
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
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .background(if (isTool) Color.LightGray else Color.White)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun KeyboardDefaultPrev() {
    KeyboardLayout(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        keyboardType = KeyboardType.Text
    )
}

@Preview
@Composable
private fun KeyboardNumberPrev() {
    KeyboardLayout(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        keyboardType = KeyboardType.Numeric(
            type = NumericKeyboardType.Number
        ),
    )
}

@Preview
@Composable
private fun KeyboardDecimalPrev() {
    KeyboardLayout(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        keyboardType = KeyboardType.Numeric(
            type = NumericKeyboardType.Decimal
        )
    )
}

@Preview
@Composable
private fun KeyboardPhonePrev() {
    KeyboardLayout(
        onKeyClick = {},
        onPaste = {},
        onBackspace = {},
        onClear = {},
        onDone = {},
        keyboardType = KeyboardType.Numeric(
            type = NumericKeyboardType.PhoneNumber
        ),
    )
}
