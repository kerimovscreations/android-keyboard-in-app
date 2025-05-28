package az.test.keyboardinapp.keyboard

import androidx.compose.runtime.Stable

sealed interface KeyboardKeys {
    data class Number(val value: Char) : KeyboardKeys
    data class Character(val value: Char) : KeyboardKeys
    data object Backspace : KeyboardKeys
    data object Space : KeyboardKeys
    data object Done : KeyboardKeys
    data object CharacterSwitch : KeyboardKeys
    data object LanguageSwitch : KeyboardKeys
    data object Shift : KeyboardKeys
}

@Stable
sealed class KeyboardType {
    data object Text: KeyboardType()
    data class Numeric(
        val type: NumericKeyboardType = NumericKeyboardType.Number,
    ) : KeyboardType()
}

enum class NumericKeyboardType {
    Number, Decimal, PhoneNumber
}

sealed interface ContentKeyType {
    data class Letter(val language: KeyboardLanguage) : ContentKeyType
    data object SpecialCharacter : ContentKeyType
}

@Stable
enum class KeyboardLanguage {
    AZ, EN, RU;
}

object KeyboardKeyMap {
    val numberRow = listOf(
        KeyboardKeys.Number('1'),
        KeyboardKeys.Number('2'),
        KeyboardKeys.Number('3'),
        KeyboardKeys.Number('4'),
        KeyboardKeys.Number('5'),
        KeyboardKeys.Number('6'),
        KeyboardKeys.Number('7'),
        KeyboardKeys.Number('8'),
        KeyboardKeys.Number('9'),
        KeyboardKeys.Number('0')
    )

    private val lettersEn = listOf(
        listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'),
        listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'),
        listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')
    )

    private val lettersAz = listOf(
        listOf('q', 'ü', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'ö', 'ğ'),
        listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ı', 'ə'),
        listOf('z', 'x', 'c', 'v', 'b', 'n', 'm', 'ç', 'ş')
    )

    private val lettersRu = listOf(
        listOf('й', 'ц', 'у', 'к', 'е', 'ё', 'н', 'г', 'ш', 'щ', 'з', 'х'),
        listOf('ф', 'ы', 'в', 'а', 'п', 'р', 'о', 'л', 'д', 'ж', 'э'),
        listOf('я', 'ч', 'с', 'м', 'и', 'т', 'ь', 'ъ', 'б', 'ю')
    )

    private val specialCharacter = listOf(
        listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')'),
        listOf('-', '+', '=', '{', '}', '[', ']', '|', '\\', ':'),
        listOf(';', '"', '\'', '<', '>', ',', '.', '_', '?', '/')
    )

    fun getContentRows(type: ContentKeyType): List<List<KeyboardKeys>> {
        val mainContent = when (type) {
            is ContentKeyType.Letter -> when (type.language) {
                KeyboardLanguage.AZ -> lettersAz
                KeyboardLanguage.EN -> lettersEn
                KeyboardLanguage.RU -> lettersRu
            }

            is ContentKeyType.SpecialCharacter -> specialCharacter
        }

        return mainContent.mapIndexed { index, row ->
            if (index != mainContent.lastIndex) {
                row.map { KeyboardKeys.Character(it) }
            } else {
                val leftKey = when (type) {
                    is ContentKeyType.Letter -> listOf(KeyboardKeys.Shift)
                    is ContentKeyType.SpecialCharacter -> emptyList()
                }

                leftKey +
                        row.map { KeyboardKeys.Character(it) } +
                        KeyboardKeys.Backspace
            }
        }
    }


    val utilityRow = listOf(
        KeyboardKeys.CharacterSwitch,
        KeyboardKeys.LanguageSwitch,
        KeyboardKeys.Space,
        KeyboardKeys.Done
    )
}
