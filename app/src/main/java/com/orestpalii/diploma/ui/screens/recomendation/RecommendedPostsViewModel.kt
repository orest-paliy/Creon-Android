package com.orestpalii.diploma.ui.screens.recomendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.service.PublicationService
import com.orestpalii.diploma.data.service.UserProfileService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendedPostsViewModel : ViewModel() {
    private val _recommendedPosts = MutableStateFlow<List<Post>>(emptyList())
    val recommendedPosts: StateFlow<List<Post>> = _recommendedPosts.asStateFlow()

    private val _recommendedLoading = MutableStateFlow(false)
    val recommendedLoading: StateFlow<Boolean> = _recommendedLoading.asStateFlow()

    fun loadRecommendedPosts(limit: Int = 10) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            _recommendedLoading.value = true
            try {
                val user = UserProfileService.fetchUserProfile(userId)
                val posts = PublicationService.fetchRecommendedPosts(user.embedding ?: emptyList(), limit)
                _recommendedPosts.value = posts
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _recommendedLoading.value = false
            }
        }
    }
}

class SubscriptionPostsViewModel : ViewModel() {
    private val _subscriptionPosts = MutableStateFlow<List<Post>>(emptyList())
    val subscriptionPosts: StateFlow<List<Post>> = _subscriptionPosts.asStateFlow()

    private val _subscriptionLoading = MutableStateFlow(false)
    val subscriptionLoading: StateFlow<Boolean> = _subscriptionLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadSubscriptionPosts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            _subscriptionLoading.value = true
            try {
                val posts = PublicationService.fetchPostsFromSubscriptions(userId)
                _subscriptionPosts.value = posts
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _subscriptionLoading.value = false
            }
        }
    }
}