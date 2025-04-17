// PostDetailViewModel.kt
package com.orestpalii.diploma.ui.screens.postDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.service.PublicationService
import com.orestpalii.diploma.data.service.UserProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {
    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post

    private val _similarPosts = MutableStateFlow<List<Post>>(emptyList())
    val similarPosts: StateFlow<List<Post>> = _similarPosts

    val currentUserId = UserProfileService.currentUserId

    fun loadPost(postId: String) {
        // Скидаємо старі дані, щоб відразу показати лоадер
        _post.value = null
        _similarPosts.value = emptyList()

        viewModelScope.launch {
            try {
                val loadedPost = PublicationService.fetchPostById(postId)
                _post.value = loadedPost

                loadedPost?.embedding?.let { emb ->
                    UserProfileService.updateUserEmbedding(emb, alpha = 0.02f)
                    _similarPosts.value = PublicationService.fetchRecommendedPosts(emb, limit = 10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // за потреби можна показати помилку
            }
        }
    }

    fun toggleLike() {
        val current = _post.value ?: return
        val updatedLiked = (current.likedBy ?: emptyList()).toMutableList()
        val wasLiked = currentUserId in updatedLiked

        if (wasLiked) updatedLiked.remove(currentUserId)
        else if (currentUserId != null) updatedLiked.add(currentUserId)

        val updatedPost = current.copy(
            likedBy = updatedLiked,
            likesCount = if (wasLiked) current.likesCount - 1 else current.likesCount + 1
        )
        _post.value = updatedPost

        viewModelScope.launch {
            PublicationService.uploadPost(updatedPost)
            if (!wasLiked) {
                updatedPost.embedding?.let {
                    UserProfileService.updateUserEmbedding(it, alpha = 0.1f)
                }
            }
        }
    }

    fun isLikedByCurrentUser(): Boolean {
        return _post.value?.likedBy?.contains(currentUserId) == true
    }
}
