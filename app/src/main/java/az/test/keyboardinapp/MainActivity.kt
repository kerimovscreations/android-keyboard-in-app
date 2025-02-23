package az.test.keyboardinapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.test.keyboardinapp.ui.theme.KeyboardInAppTheme
import kotlinx.coroutines.awaitCancellation

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyboardInAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerSpace ->
                    Box(modifier = Modifier.padding(innerSpace)) {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var text by remember {
        mutableStateOf(
            TextFieldValue(
                ""
            )
        )
    }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        DisableSoftKeyboard(disable = true) {
            TextField(
                value = text,
                placeholder = { Text("Enter text") },
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isKeyboardVisible = focusState.isFocused
                    }
            )
        }


        if (isKeyboardVisible) {
            CustomKeyboard(
                onKeyClick = { keyChar ->
                    val newText = text.text + keyChar
                    text = TextFieldValue(newText, TextRange(newText.length))
                },
                onBackspace = {
                    if (text.text.isNotEmpty()) {
                        val newText = text.text.dropLast(1)
                        text = TextFieldValue(newText, TextRange(newText.length))
                    }
                },
                onComplete = {
                    isKeyboardVisible = false
                    focusManager.clearFocus()
                },
                onClear = {
                    if (text.text.isNotEmpty()) {
                        text = TextFieldValue("", TextRange(0))
                    }
                }
            )
        }
    }
}

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
fun CustomKeyboard(
    onKeyClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onComplete: () -> Unit
) {
    var uppercased by remember { mutableStateOf(false) }
    var contentKeyType by remember { mutableStateOf(ContentKeyType.LETTER) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            KeyboardKeyMap.numberRow.forEach {
                val key = it.value.toString().first()
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
                            ContentKeyType.LETTER -> "123"
                            ContentKeyType.SPECIAL_CHARACTER -> "ABC"
                        }
                        TextButton(
                            text = keyText,
                            isRounded = true,
                            onClick = {
                                contentKeyType = if (contentKeyType == ContentKeyType.LETTER) {
                                    ContentKeyType.SPECIAL_CHARACTER
                                } else {
                                    ContentKeyType.LETTER
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
                            iconId = R.drawable.ic_done,
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
}

@Composable
fun CharacterButton(
    char: Char,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
        modifier = modifier
    )
}

@Composable
fun TextButton(
    text: String,
    isRounded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
        isRounded = isRounded
    )
}

@Composable
fun IconButton(
    iconId: Int,
    isRounded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
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
        isRounded = isRounded
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isRounded: Boolean = false,
    isTool: Boolean = false
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(44.dp)
            .clip(if (isRounded) RoundedCornerShape(22.dp) else RoundedCornerShape(4.dp))
            .combinedClickable(
                enabled = true,
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