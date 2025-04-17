package com.orestpalii.diploma.ui.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orestpalii.diploma.data.model.Post

@Composable
fun SubscriptionPostsList(posts: List<Post>, selectedPost: (Post) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts.size) { index ->
                PostRowView(
                    post = posts[index],
                    onClick = { selectedPost(posts[index]) }
                )
            }
        }
    }
}
