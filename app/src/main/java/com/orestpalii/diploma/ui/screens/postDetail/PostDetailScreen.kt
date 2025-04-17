package com.orestpalii.diploma.ui.screens.postDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.ui.helper.PinterestGridWrapper
import com.orestpalii.diploma.ui.helper.ZoomableImageScreen
import com.orestpalii.diploma.ui.screens.comments.CommentsScreen

@Composable
fun PostDetailScreen(
    postId: String,
    onPostSelected: (Post) -> Unit,
    onShowProfile: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = viewModel()
) {
    val post by viewModel.post.collectAsState()
    val similarPosts by viewModel.similarPosts.collectAsState()

    var loading by remember { mutableStateOf(true) }
    var imageRatio by remember { mutableStateOf(1f) }
    var showImageFullscreen by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }

    // Запускаємо завантаження і скидаємо стан
    LaunchedEffect(postId) {
        loading = true
        viewModel.loadPost(postId)
    }
    // Коли прийшов пост із очікуваним id — знімаємо лоадер
    LaunchedEffect(post) {
        if (post?.id == postId) {
            loading = false
        }
    }

    // Якщо досі вантажимо — показуємо лоадер
    if (loading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Далі, коли не loading:
    if (showImageFullscreen) {
        post?.imageUrl?.let {
            ZoomableImageScreen(imageUrl = it, onClose = { showImageFullscreen = false })
        }
    } else if (showComments && post != null) {
        key(post!!.id) {
            CommentsScreen(postId = post!!.id, onClose = { showComments = false })
        }
    } else {
        post?.let { currentPost ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = currentPost.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(imageRatio)
                ) {
                    AsyncImage(
                        model            = post?.imageUrl,
                        contentDescription = null,
                        contentScale     = ContentScale.Fit,
                        modifier         = Modifier
                            .fillMaxSize()
                            .clickable { showImageFullscreen = true },
                        onSuccess = { success ->
                            val drawable = success.result.drawable
                            val w = drawable.intrinsicWidth
                            val h = drawable.intrinsicHeight
                            if (w > 0 && h > 0) {
                                imageRatio = w.toFloat() / h
                            }
                        }
                    )
                    if (currentPost.isAIgenerated) {
                        Text(
                            text = "AI Generated   ".repeat(20),
                            overflow = TextOverflow.Clip,
                            softWrap = false,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(vertical = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (currentPost.authorId != viewModel.currentUserId) {
                            IconButton(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                onClick = { onShowProfile(currentPost.authorId) }
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Автор", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            onClick = { viewModel.toggleLike() }
                        ) {
                            Icon(
                                imageVector = if (viewModel.isLikedByCurrentUser()) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Лайк",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            onClick = { showComments = true }
                        ) {
                            Icon(Icons.Default.Comment, contentDescription = "Коментарі", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .width(45.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                                .padding(horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("AI", color = MaterialTheme.colorScheme.primary)
                            Text("${currentPost.aiConfidence}%", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (currentPost.description.isNotBlank()) {
                        Text(
                            text = currentPost.description,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider()
                    }
                    Text(
                        "Опис зображення згенерований AI",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = currentPost.tags,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Схожі публікації",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                PinterestGridWrapper(
                    posts = similarPosts,
                    onPostClick = onPostSelected,
                    paddingTop = 8,
                    paddingBottom = 0,
                    isScrollable = false
                )
            }
        }
    }
}
