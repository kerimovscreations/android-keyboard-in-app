package az.test.keyboardinapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import az.test.keyboardinapp.keyboard.AnimatedKeyboard
import az.test.keyboardinapp.keyboard.DisableSoftKeyboard
import az.test.keyboardinapp.keyboard.KeyboardType
import az.test.keyboardinapp.keyboard.PasteUsageRule
import az.test.keyboardinapp.ui.theme.KeyboardInAppTheme
import az.test.keyboardinapp.visual_transformations.PanVisualTransformation
import az.test.keyboardinapp.visual_transformations.PhoneNumberVisualTransformation

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

@Stable
enum class EditTextType {
    Text, Pan, Number, Decimal, PhoneNumber
}

fun addClipboardText(clipboardManager: ClipboardManager, type: EditTextType){
    when(type){
        EditTextType.Text -> clipboardManager.setText(buildAnnotatedString { append("Some random text") })
        EditTextType.Pan -> clipboardManager.setText(buildAnnotatedString { append("5432 1234 5678 9876") })
        EditTextType.PhoneNumber -> clipboardManager.setText(buildAnnotatedString { append(listOf("+994 12 345 67 89","994 12 345 67 89","012 345 67 89", "12 345 67 89").shuffled()[0]) })
        else -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen() {
    var selectedEditTextType by remember { mutableStateOf(EditTextType.Text) }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.Center
        ) {
            EditTextType.entries.forEach {
                val isSelected by remember { derivedStateOf { it == selectedEditTextType } }
                FilterChip(
                    selected = isSelected,
                    label = { Text(text = it.name) },
                    shape = CircleShape,
                    onClick = {
                        if (isSelected) return@FilterChip

                        selectedEditTextType = it
                        text = TextFieldValue("")
                        addClipboardText(clipboardManager, it)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            DisableSoftKeyboard(disable = true) {
                TextField(
                    value = text,
                    placeholder = { Text("Enter ${selectedEditTextType.name}") },
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isKeyboardVisible = focusState.isFocused
                        },
                    visualTransformation = when(selectedEditTextType){
                        EditTextType.Pan -> PanVisualTransformation()
                        EditTextType.PhoneNumber -> PhoneNumberVisualTransformation()
                        else -> VisualTransformation.None
                    }
                )
            }

        }

        AnimatedKeyboard(
            isKeyboardVisible = isKeyboardVisible,
            keyboardType = when (selectedEditTextType) {
                EditTextType.Text -> KeyboardType.Text
                EditTextType.Pan, EditTextType.Number -> KeyboardType.Number
                EditTextType.Decimal -> KeyboardType.Decimal
                EditTextType.PhoneNumber -> KeyboardType.PhoneNumber
            },
            pasteUsageRule = when (selectedEditTextType) {
                EditTextType.Text -> PasteUsageRule.All
                EditTextType.Pan -> PasteUsageRule.CardPan
                EditTextType.Number, EditTextType.Decimal -> PasteUsageRule.None
                EditTextType.PhoneNumber -> PasteUsageRule.PhoneNumber
            },
            onKeyClick = { keyChar ->
                val newText = text.text + keyChar
                text = TextFieldValue(newText, TextRange(newText.length))
            },
            onPaste = {
                text = TextFieldValue(it, TextRange(it.length))
            },
            onBackspace = {
                if (text.text.isNotEmpty()) {
                    val newText = text.text.dropLast(1)
                    text = TextFieldValue(newText, TextRange(newText.length))
                }
            },
            onDone = {
                isKeyboardVisible = false
                focusManager.clearFocus()
            },
            onClose = {
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