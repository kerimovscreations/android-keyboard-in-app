package az.test.keyboardinapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import az.test.keyboardinapp.keyboard.DisableSoftKeyboard
import az.test.keyboardinapp.keyboard.InAppKeyboard
import az.test.keyboardinapp.keyboard.KeyboardType
import az.test.keyboardinapp.keyboard.NumericKeyboardType
import az.test.keyboardinapp.ui.theme.KeyboardInAppTheme
import kotlinx.coroutines.runBlocking

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
    Text, Number, Decimal, PhoneNumber
}

fun addClipboardText(clipboard: Clipboard, type: EditTextType) {
    val text = when (type) {
        EditTextType.Text -> "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis,."
        EditTextType.PhoneNumber ->
            listOf(
                "+994 12 345 67 89",
                "994 12 345 67 89",
                "012 345 67 89",
                "12 345 67 89"
            ).shuffled()[0]

        else -> ""
    }
    val clip: ClipData = ClipData.newPlainText("In app keyboard clipboard", text)

    runBlocking {
        clipboard.setClipEntry(
            ClipEntry(
                clip
            )
        )
    }
}

@Composable
fun MainScreen() {
    var selectedEditTextType by remember { mutableStateOf(EditTextType.Text) }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val clipboard = LocalClipboard.current

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
                        addClipboardText(clipboard, it)
                    }
                )
            }
        }

        DisableSoftKeyboard {
            TextField(
                value = text,
                placeholder = { Text("Enter ${selectedEditTextType.name}") },
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isKeyboardVisible = focusState.isFocused
                    },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        InAppKeyboard(
            visible = isKeyboardVisible,
            keyboardType = when (selectedEditTextType) {
                EditTextType.Text -> KeyboardType.Text
                EditTextType.Number -> KeyboardType.Numeric(NumericKeyboardType.Number)
                EditTextType.Decimal -> KeyboardType.Numeric(NumericKeyboardType.Decimal)
                EditTextType.PhoneNumber -> KeyboardType.Numeric(NumericKeyboardType.PhoneNumber)
            },
            textFieldValue = text,
            onTextFieldValueChange = { text = it },
            onDone = {
                isKeyboardVisible = false
                focusManager.clearFocus()
            },
            onClose = {
                isKeyboardVisible = false
                focusManager.clearFocus()
            },
            clipboard = clipboard
        )
    }
}