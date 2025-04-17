package com.orestpalii.diploma.ui.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DecoratedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    // Збільшуємо шрифт до bodyLarge
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    ),
    placeholderStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    ),
    cursorColor: SolidColor = SolidColor(MaterialTheme.colorScheme.primary),
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    minHeight: Dp = 56.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clip(shape)
            .background(backgroundColor)
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            cursorBrush = cursorColor,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(text = placeholder, style = placeholderStyle)
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DecoratedTextFieldPreview() {
    var text by remember { mutableStateOf("") }
    Surface {
        Column(modifier = Modifier.padding(16.dp)) {
            DecoratedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Промпт для генерації"
            )
            // Можете вставити ще одну для демонстрації з уже введеним текстом
            DecoratedTextField(
                modifier = Modifier.padding(top = 16.dp),
                value = "Приклад тексту",
                onValueChange = {},
                placeholder = "Промпт для генерації"
            )
        }
    }
}
