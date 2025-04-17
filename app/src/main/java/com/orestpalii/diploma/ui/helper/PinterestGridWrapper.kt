package com.orestpalii.diploma.ui.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orestpalii.diploma.data.model.Post

@Composable
fun PinterestGridWrapper(
    posts: List<Post>,
    paddingTop: Int,
    paddingBottom: Int = 90,
    onPostClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = true
) {
    val scrollModifier = if (isScrollable) {
        modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 0.dp)
    } else {
        modifier.padding(horizontal = 20.dp, vertical = 0.dp)
    }

    Column(
        modifier = scrollModifier
    ) {
        PinterestGrid(
            posts = posts,
            onPostClick = onPostClick,
            modifier = Modifier.fillMaxWidth()
                .padding(top = paddingTop.dp)
        )
        if(paddingBottom != 0) {
            Spacer(modifier = Modifier.height(paddingBottom.dp))
        }
    }
}
