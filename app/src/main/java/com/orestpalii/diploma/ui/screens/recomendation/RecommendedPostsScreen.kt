package com.orestpalii.diploma.ui.screens.recomendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.ui.helper.PinterestGridWrapper
import com.orestpalii.diploma.ui.helper.SubscriptionPostsList

enum class PostSource { RECOMMENDED, SUBSCRIPTIONS }

@Composable
fun RecommendedPostsScreen(
    selectedPost: (Post) -> Unit,
    recommendedVM: RecommendedPostsViewModel = viewModel(),
    subscriptionsVM: SubscriptionPostsViewModel = viewModel()
) {


    var selectedSource by remember { mutableStateOf(PostSource.RECOMMENDED) }

    val posts = when (selectedSource) {
        PostSource.RECOMMENDED -> recommendedVM.recommendedPosts.collectAsState().value
        PostSource.SUBSCRIPTIONS -> subscriptionsVM.subscriptionPosts.collectAsState().value
    }

    val isLoading = when (selectedSource) {
        PostSource.RECOMMENDED -> recommendedVM.recommendedLoading.collectAsState().value
        PostSource.SUBSCRIPTIONS -> subscriptionsVM.subscriptionLoading.collectAsState().value
    }

    LaunchedEffect(selectedSource) {
        when (selectedSource) {
            PostSource.RECOMMENDED -> recommendedVM.loadRecommendedPosts()
            PostSource.SUBSCRIPTIONS -> subscriptionsVM.loadSubscriptionPosts()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SourceToggleButton(
                icon = if (selectedSource == PostSource.RECOMMENDED) Icons.Default.Star else Icons.Default.StarOutline,
                text = "Рекомендовані",
                isSelected = selectedSource == PostSource.RECOMMENDED,
                onClick = { selectedSource = PostSource.RECOMMENDED }
            )
            Spacer(Modifier.width(12.dp))
            SourceToggleButton(
                icon = if (selectedSource == PostSource.SUBSCRIPTIONS) Icons.Default.Group else Icons.Default.GroupAdd,
                text = "Підписки",
                isSelected = selectedSource == PostSource.SUBSCRIPTIONS,
                onClick = { selectedSource = PostSource.SUBSCRIPTIONS }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (!isLoading && posts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Наме не вдалось нічого завантажити",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            else if (isLoading && posts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = when (selectedSource) {
                        PostSource.RECOMMENDED -> "Завантажуємо ваші рекомендації..."
                        PostSource.SUBSCRIPTIONS -> "Завантажуємо пости з підписок..."
                    }, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                if (selectedSource == PostSource.RECOMMENDED) {
                    PinterestGridWrapper(
                        posts = posts, onPostClick = selectedPost,
                        paddingTop = 0,
                        modifier = Modifier
                    )
                } else {
                    SubscriptionPostsList(posts) { post ->
                        selectedPost(post)
                    }
                }
            }
        }
    }
}

@Composable
fun SourceToggleButton(icon: ImageVector, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text)
    }
}