package az.test.keyboardinapp.visual_transformations

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PanVisualTransformation() : VisualTransformation {


    override fun filter(text: AnnotatedString): TransformedText {

        val filtered = text.text.filterNot { it.isWhitespace() }

        val trimmed = if (filtered.length >=  16) filtered.substring(0..15) else filtered

        val formattedText = buildString {
            trimmed.forEachIndexed { index, char ->
                if (index > 0 && index % 4 == 0) append(" ") // Insert space every 4 characters
                append(char)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {         
                val transformedOffset = when (offset) {
                    in 0..4 -> offset
                    in 5..8 -> offset + 1
                    in 9..12 -> offset + 2
                    in 13..16 -> offset + 3
                    else -> 19
                }

                return transformedOffset
            }      
            override fun transformedToOriginal(offset: Int): Int {         
                if (offset <= 4) return offset         
                if (offset <= 9) return offset - 1         
                if (offset <= 14) return offset - 2         
                if (offset <= 19) return offset - 3         
                return 16     
            }
        }

        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = offsetMapping
        )
    }
}


