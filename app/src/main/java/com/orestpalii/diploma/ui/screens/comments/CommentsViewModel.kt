package com.orestpalii.diploma.ui.screens.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.orestpalii.diploma.data.model.Comment
import com.orestpalii.diploma.data.service.CommentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CommentsViewModel(private val postId: String) : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _newComment = MutableStateFlow("")
    val newComment: StateFlow<String> = _newComment

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    init {
        fetchComments()
    }

    fun fetchComments() {
        viewModelScope.launch {
            try {
                val fetched = CommentService.fetchComments(postId)
                _comments.value = fetched.sortedByDescending { it.likedBy.size }
            } catch (e: Exception) {
                _errorMessage.value = "Помилка при завантаженні: ${e.localizedMessage}"
            }
        }
    }

    fun sendComment() {
        val text = _newComment.value.trim()
        if (text.isBlank()) return

        val comment = Comment(
            id = UUID.randomUUID().toString(),
            userId = userId,
            text = text,
            createdAt = System.currentTimeMillis().toDouble(),
            likedBy = emptyList()
        )

        viewModelScope.launch {
            try {
                val updatedComments = listOf(comment) + _comments.value
                CommentService.saveComments(updatedComments, postId)
                _newComment.value = ""
                fetchComments()
            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося надіслати коментар: ${e.localizedMessage}"
            }
        }
    }

    fun toggleLike(commentId: String) {
        viewModelScope.launch {
            val updated = _comments.value.map {
                if (it.id == commentId) {
                    val newLikes = if (it.likedBy.contains(userId)) {
                        it.likedBy - userId
                    } else {
                        it.likedBy + userId
                    }
                    it.copy(likedBy = newLikes)
                } else it
            }
            CommentService.saveComments(updated, postId)
            fetchComments()
        }
    }

    fun setNewComment(value: String) {
        _newComment.value = value
    }
}

class CommentsViewModelFactory(private val postId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommentsViewModel(postId) as T
    }
}
