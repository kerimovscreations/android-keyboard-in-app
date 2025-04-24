package az.test.keyboardinapp.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager

@Stable
enum class PasteUsageRule {
    None, All, PhoneNumber, CardPan, DomesticIban, Swift, ForeignIban
}

@Composable
inline fun PasteRuleValidatedContent(
    rule: PasteUsageRule,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    content: @Composable (String) -> Unit,
) {
    val clipboardText = clipboardManager.getText()?.text?.ifBlank { return } ?: return

    when (rule) {
        PasteUsageRule.All -> {
            val text = clipboardText.trim()
            content(text)
        }

        PasteUsageRule.CardPan -> {
            clipboardText.cardPanPasteRule { content(it) }
        }

        PasteUsageRule.PhoneNumber -> {
            clipboardText.phoneNumberPasteRule { content(it) }
        }

        PasteUsageRule.DomesticIban -> {
            clipboardText.domesticIbanPasteRule { content(it) }
        }

        PasteUsageRule.Swift -> {
            clipboardText.accountSwiftPasteRule { content(it) }
        }

        PasteUsageRule.ForeignIban -> {
            clipboardText.foreignIbanPasteRule { content(it) }
        }

        PasteUsageRule.None -> return
    }
}


inline fun String.phoneNumberPasteRule(isCountryCodeIncluded: Boolean = true, onValid: (String) -> Unit) {

    val isPossiblePhoneNumber = this.all { it.isDigit() || it == ' ' || it == '+' }
    if (isPossiblePhoneNumber.not()) return

    val numberText = this.filter { it.isDigit() }
    if (numberText.length > 12 || numberText.isEmpty()) return

    var phoneNumber: String? = null
    when (numberText.length) {
        12 -> {
            if (numberText.startsWith("994").not()) return
            phoneNumber = numberText.substring(3)
        }

        10 -> {
            if (numberText.startsWith("0").not()) return
            phoneNumber = numberText.substring(1)
        }

        9 -> {
            phoneNumber = numberText
        }
    }

    phoneNumber?.let {
        if (isCountryCodeIncluded) {
            onValid("+994$it")
        } else onValid(it)
    }
}

inline fun String.cardPanPasteRule(onValid: (String) -> Unit) {
    val isPossibleCardPan = this.all { it.isDigit() || it == ' ' }
    if (isPossibleCardPan.not()) return

    val cardPan = this.filter { it.isDigit() }
    if (cardPan.length != 16) return

    onValid(cardPan)
}

inline fun String.domesticIbanPasteRule(onValid: (String) -> Unit) {
    val filteredText = this.filter { it.isLetterOrDigit() }.ifBlank { return }

    val isAllLetterCapitalized = filteredText.filter { it.isLetter() }.all { it.isUpperCase() }

    if (isAllLetterCapitalized && filteredText.length <= 28) onValid(filteredText)
}

inline fun String.accountSwiftPasteRule(onValid: (String) -> Unit) {
    val filteredText = this.filter { it.isLetterOrDigit() }.ifBlank { return }

    val isAllLetterCapitalized = filteredText.filter { it.isLetter() }.all { it.isUpperCase() }

    if (isAllLetterCapitalized && filteredText.length <= 11) onValid(filteredText)
}

inline fun String.foreignIbanPasteRule(onValid: (String) -> Unit) {
    val filteredText = this.filter { it.isLetterOrDigit() }.ifBlank { return }

    val isAllLetterCapitalized = filteredText.filter { it.isLetter() }.all { it.isUpperCase() }

    if (isAllLetterCapitalized && filteredText.length <= 34) onValid(filteredText)
}