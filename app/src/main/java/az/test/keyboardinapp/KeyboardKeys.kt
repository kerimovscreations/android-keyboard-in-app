package az.test.keyboardinapp

sealed class KeyboardKeys {
    data class Number(val value: Int) : KeyboardKeys()
    data class Character(val value: Char) : KeyboardKeys()
    data object Backspace : KeyboardKeys()
    data object Space : KeyboardKeys()
    data object Done : KeyboardKeys()
    data object CharacterSwitch : KeyboardKeys()
    data object Shift : KeyboardKeys()
}

enum class ContentKeyType {
    LETTER,
    SPECIAL_CHARACTER
}

object KeyboardKeyMap {
    val numberRow = listOf(
        KeyboardKeys.Number(1),
        KeyboardKeys.Number(2),
        KeyboardKeys.Number(3),
        KeyboardKeys.Number(4),
        KeyboardKeys.Number(5),
        KeyboardKeys.Number(6),
        KeyboardKeys.Number(7),
        KeyboardKeys.Number(8),
        KeyboardKeys.Number(9),
        KeyboardKeys.Number(0)
    )

    private val letters = listOf(
        listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'),
        listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'),
        listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')
    )

    private val specialCharacter = listOf(
        listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')'),
        listOf('-', '+', '=', '{', '}', '[', ']', '|', '\\', ':'),
        listOf(';', '"', '\'', '<', '>', ',', '.', '_', '?', '/')
    )

    fun getContentRows(type: ContentKeyType): List<List<KeyboardKeys>> {
        val mainContent = when (type) {
            ContentKeyType.LETTER -> letters
            ContentKeyType.SPECIAL_CHARACTER -> specialCharacter
        }

        return mainContent.mapIndexed { index, row ->
            if (index != 2) {
                row.map { KeyboardKeys.Character(it) }
            } else {
                val leftKey = when (type) {
                    ContentKeyType.LETTER -> listOf(KeyboardKeys.Shift)
                    ContentKeyType.SPECIAL_CHARACTER -> emptyList()
                }

                leftKey +
                        row.map { KeyboardKeys.Character(it) } +
                        KeyboardKeys.Backspace
            }
        }
    }


    val utilityRow = listOf(
        KeyboardKeys.CharacterSwitch,
        KeyboardKeys.Space,
        KeyboardKeys.Done
    )
}