package com.orestpalii.diploma.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.orestpalii.diploma.ui.helper.CommentInputField
import com.orestpalii.diploma.ui.helper.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(postId: String, onClose: () -> Unit) {
    val viewModel: CommentsViewModel = viewModel(
        key = "comments_$postId",
        factory = CommentsViewModelFactory(postId)
    )
    val comments by viewModel.comments.collectAsState()
    val newComment by viewModel.newComment.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Column(modifier = Modifier
        .fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        TopAppBar(
            title = { Text("Коментарі") },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(comments) { comment ->
                val isLiked = currentUserId?.let { comment.likedBy.contains(it) } ?: false

                CommentItem(
                    comment = comment,
                    onLike = {
                        viewModel.toggleLike(comment.id)
                    },
                    isLiked = isLiked
                )
            }
        }

        CommentInputField(
            newComment = viewModel.newComment.collectAsState().value,
            onCommentChange = viewModel::setNewComment,
            onSend = { viewModel.sendComment() }
        )
    }
}


