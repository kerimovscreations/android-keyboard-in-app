# Custom In-App Keyboard for Android

This project provides a customizable in-app keyboard component for Android applications, built with Jetpack Compose. It allows developers to offer a tailored input experience, replacing the standard system keyboard within specific parts of their application.

## Features

- **Custom In-App Keyboard Component:** Provides a fully custom keyboard experience within your app.
- **Multiple Keyboard Types:** Supports various input scenarios:
    - `Text`: For general text input, including multi-language support (English, Azerbaijani, Russian) and special characters.
    - `Number`: For numerical input.
    - `Decimal`: For numbers that include a decimal point.
    - `PhoneNumber`: Tailored for phone number entry, allowing `+` and spaces.
    - `PAN`: Designed for payment card PAN entry (formats input as `XXXX XXXX XXXX XXXX`).
- **Visual Transformations:** Includes built-in transformations for:
    - PAN formatting.
    - Phone number formatting.
- **Configurable Paste Functionality:**
    - Control paste actions based on the input field type (e.g., allow pasting only valid card numbers into a PAN field).
    - Supports rules like `All`, `CardPan`, `PhoneNumber`, `None`.
- **Jetpack Compose:** Built entirely with Jetpack Compose for modern Android UI development.
- **System Keyboard Suppression:** Automatically hides the default Android soft keyboard when the custom keyboard is active.
- **Haptic Feedback:** Provides tactile feedback on key presses.

## Integration

To integrate the custom keyboard into your Jetpack Compose screen:

1.  **Disable System Soft Keyboard:**
    Wrap your `TextField` (or any Composable that takes focus) with the `DisableSoftKeyboard` Composable. This prevents the standard Android keyboard from appearing.

    ```kotlin
    import az.test.keyboardinapp.keyboard.DisableSoftKeyboard

    DisableSoftKeyboard {
        TextField(
            value = textState,
            onValueChange = { textState = it },
            // ... other TextField parameters
        )
    }
    ```

2.  **Add the `AnimatedKeyboard` Composable:**
    Place the `AnimatedKeyboard` Composable in your screen layout. It will typically be anchored to the bottom.

    ```kotlin
    import az.test.keyboardinapp.keyboard.AnimatedKeyboard
    import az.test.keyboardinapp.keyboard.KeyboardType
    import az.test.keyboardinapp.keyboard.PasteUsageRule
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.setValue
    // ... other necessary imports

    var isKeyboardVisible by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current

    // ... (In your Column or Box)

    AnimatedKeyboard(
        isKeyboardVisible = isKeyboardVisible, // Control visibility based on TextField focus
        keyboardType = KeyboardType.Text,    // Or KeyboardType.Number, .Decimal, .PhoneNumber
        pasteUsageRule = PasteUsageRule.All, // Or .CardPan, .PhoneNumber, .None
        onKeyClick = { char ->
            val newText = textFieldValue.text + char
            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
        },
        onPaste = { pastedText ->
            textFieldValue = TextFieldValue(pastedText, TextRange(pastedText.length))
        },
        onBackspace = {
            if (textFieldValue.text.isNotEmpty()) {
                val newText = textFieldValue.text.dropLast(1)
                textFieldValue = TextFieldValue(newText, TextRange(newText.length))
            }
        },
        onDone = {
            isKeyboardVisible = false
            focusManager.clearFocus() // Example: Hide keyboard and clear focus
        },
        onClose = {
            isKeyboardVisible = false
            focusManager.clearFocus() // Example: Hide keyboard and clear focus
        },
        onClear = {
            textFieldValue = TextFieldValue("", TextRange(0))
        }
    )
    ```

3.  **Manage Keyboard Visibility:**
    You'll need to control the `isKeyboardVisible` state, typically by observing the focus state of your `TextField`.

    ```kotlin
    TextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = Modifier
            .onFocusChanged { focusState ->
                isKeyboardVisible = focusState.isFocused
            }
        // ...
    )
    ```

### Key Parameters for `AnimatedKeyboard`:

*   `isKeyboardVisible: Boolean`: Controls the visibility of the keyboard.
*   `keyboardType: KeyboardType`: Defines the layout and functionality (e.g., `Text`, `Number`, `Decimal`, `PhoneNumber`).
*   `pasteUsageRule: PasteUsageRule`: Sets the rules for what content can be pasted from the clipboard.
*   `onKeyClick: (Char) -> Unit`: Callback when a character key is pressed.
*   `onPaste: (String) -> Unit`: Callback when the paste key is used. Provides the validated text from the clipboard.
*   `onBackspace: () -> Unit`: Callback for the backspace key.
*   `onClear: () -> Unit`: Callback for long-pressing backspace (clears the field).
*   `onDone: () -> Unit`: Callback for the "Done" or "Enter" key (behavior can be customized).
*   `onClose: () -> Unit`: Callback when the keyboard is dismissed (e.g., via back press).

## Usage Examples

The `MainActivity.kt` in the sample app demonstrates how to dynamically change the keyboard type, visual transformation, and paste rules based on user selection. Here's a conceptual overview:

```kotlin
// (Simplified example inspired by MainActivity.kt)

enum class EditTextType {
    Text, Pan, Number, Decimal, PhoneNumber
}

@Composable
fun MyScreenWithDynamicKeyboard() {
    var selectedEditTextType by remember { mutableStateOf(EditTextType.Text) }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var isKeyboardVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // ... (UI to select EditTextType, e.g., FilterChip)

    DisableSoftKeyboard {
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
                EditTextType.Pan -> PanVisualTransformation() // Custom visual transformation
                EditTextType.PhoneNumber -> PhoneNumberVisualTransformation() // Custom visual transformation
                else -> VisualTransformation.None
            }
        )
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
            EditTextType.Pan -> PasteUsageRule.CardPan // Only allows valid PAN patterns
            EditTextType.Number, EditTextType.Decimal -> PasteUsageRule.None // No pasting
            EditTextType.PhoneNumber -> PasteUsageRule.PhoneNumber // Allows phone number patterns
        },
        // ... other callbacks: onKeyClick, onPaste, onBackspace, onDone, onClose, onClear
        onKeyClick = { /* ... */ },
        onPaste = { /* ... */ },
        onBackspace = { /* ... */ },
        onDone = {
            isKeyboardVisible = false
            focusManager.clearFocus()
        },
        onClose = {
            isKeyboardVisible = false
            focusManager.clearFocus()
        },
        onClear = { /* ... */ }
    )
}
```

This example illustrates how to:
- Maintain state for the current `EditTextType`.
- Update the `TextField`'s `visualTransformation` based on the selected type.
- Configure the `AnimatedKeyboard`'s `keyboardType` and `pasteUsageRule` dynamically.

Refer to `MainActivity.kt` for a runnable example.

## Limitations

- **In-App Scope:** This is an in-app keyboard solution and does not function as a system-wide Input Method Editor (IME). It's designed to be used within the application it's integrated into.
- **Advanced Text Editing:** Current version has limited support for advanced text editing features directly via the keyboard, such as:
    - Text selection within the input field using keyboard gestures.
    - Fine-grained cursor movement using keyboard keys.
    These actions typically rely on the native `TextField` capabilities.
- **Visual Customization:** The keyboard's visual appearance (colors, themes, key styles) is currently defined within its Composable structure. Extensive theming or dynamic visual customization options are not provided out-of-the-box and would require direct modification of the keyboard's Composable code.
- **Accessibility (A11y):** While haptic feedback is implemented for key presses, comprehensive accessibility features (e.g., full compatibility with screen readers like TalkBack for the custom keyboard elements) may require further development and testing.
- **Pre-defined Layouts:** Keyboard layouts for different languages and types are currently hardcoded. Adding entirely new languages or significantly altering layouts requires code modification.

## Extending the Keyboard

The keyboard is designed to be extensible. Here are some ways you can adapt or add functionality:

### 1. Adding New `KeyboardType`s

- **Define Enum:** Add a new entry to the `KeyboardType` enum located in `keyboard/KeyboardKeys.kt` (or a similar file).
- **Create Keyboard Composable (if needed):** If the new type requires a significantly different layout or logic, create a new Composable function similar to `TextKeyboard` or `NumberKeyboard` in `keyboard/Keyboard.kt`.
- **Update `CustomKeyboard`:** Modify the `when` statement within the `CustomKeyboard` Composable in `keyboard/Keyboard.kt` to delegate to your new keyboard Composable when your new `KeyboardType` is active.
- **Integrate:** Update your application logic (e.g., in `MainActivity` or your custom screens) to allow selection and usage of the new `KeyboardType`.

### 2. Implementing New `PasteUsageRule`s

- **Define Enum:** Add a new entry to the `PasteUsageRule` enum in `keyboard/KeyboardPasteRules.kt`.
- **Implement Logic:** The core paste validation logic resides in the `PasteRuleValidatedContent` Composable and its associated functions like `validateTextForPaste` in `keyboard/KeyboardPasteRules.kt`. You'll need to:
    - Add a case for your new rule in `validateTextForPaste` (or a similar validation function) to define what text patterns are considered valid for this rule.
    - Ensure `PasteRuleValidatedContent` correctly handles the display and interaction based on your new rule.

### 3. Creating Custom `VisualTransformation`s

- **Implement Interface:** Create a new Kotlin class that implements the `androidx.compose.ui.text.input.VisualTransformation` interface.
- **Define `filter` method:** Implement the `filter` method to transform the `AnnotatedString` input into the desired visual output.
- **Apply:** Use your custom transformation in the `visualTransformation` parameter of your `TextField`.
  ```kotlin
  TextField(
      // ...
      visualTransformation = MyCustomVisualTransformation()
  )
  ```
  Examples like `PanVisualTransformation.kt` and `PhoneNumberVisualTransformation.kt` can be found in the `visual_transformations` directory.

### 4. Modifying Keyboard Layouts or Adding Languages

- **Text Keyboard (`TextKeyboard`):**
    - **Languages:** To add a new language, you'll need to:
        - Add a new entry to the `KeyboardLanguage` enum in `keyboard/KeyboardKeys.kt`.
        - Update `KeyboardKeyMap.getContentRows` to return the character layout for your new language when it's active. This might involve defining new character sets within `KeyboardKeyMap`.
        - Modify the language switching logic in `TextKeyboard` to include the new language.
    - **Key Layout:** Changes to the QWERTY layout, special characters, or the arrangement of number rows would involve modifying the `KeyboardKeyMap` object and potentially the `TextKeyboard` Composable's layout logic.
- **Number Keyboards (`NumberKeyboard`):**
    - The layouts for `Number`, `Decimal`, and `PhoneNumber` are more directly structured in the `NumberKeyboard` Composable. Modifications would involve changing the `Row` and `CharacterButton` arrangements within this function.

Remember to thoroughly test any extensions or modifications.

## Contributing

Contributions are welcome! If you have suggestions for improvements, new features, or find any bugs, please feel free to:

- Open an issue on the project's repository.
- Fork the repository, make your changes, and submit a pull request.

We appreciate your help in making this custom keyboard even better.
