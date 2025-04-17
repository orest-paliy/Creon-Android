package com.orestpalii.diploma.ui.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orestpalii.diploma.data.model.Post

@Composable
fun PinterestGrid(modifier: Modifier = Modifier,
                  columns: Int = 2,
                  spacing: Dp = 8.dp,
                  posts: List<Post>,
                  onPostClick: (Post) -> Unit
) {
    val itemWidth = remember {
        mutableStateOf(0)
    }

    Layout(
        content = {
            posts.forEach { post ->
                PostRowView(post = post, onClick = { onPostClick(post) })
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val columnWidth = (constraints.maxWidth - (spacing.roundToPx() * (columns - 1))) / columns
        itemWidth.value = columnWidth

        val columnHeights = IntArray(columns) { 0 }
        val placeables = measurables.map { measurable ->
            val minColumn = columnHeights.withIndex().minByOrNull { it.value }!!.index
            val placeable = measurable.measure(
                Constraints.fixedWidth(columnWidth)
            )
            columnHeights[minColumn] += placeable.height + spacing.roundToPx()
            Pair(placeable, minColumn)
        }

        val height = columnHeights.maxOrNull()?.coerceAtLeast(constraints.minHeight) ?: constraints.minHeight

        layout(constraints.maxWidth, height) {
            val yOffsets = IntArray(columns) { 0 }

            placeables.forEach { (placeable, column) ->
                val x = (columnWidth + spacing.roundToPx()) * column
                val y = yOffsets[column]
                placeable.placeRelative(x = x, y = y)
                yOffsets[column] += placeable.height + spacing.roundToPx()
            }
        }
    }
}