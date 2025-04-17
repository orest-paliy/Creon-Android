package com.orestpalii.diploma.ui.screens.userProfile

import android.graphics.BitmapFactory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.model.User
import com.orestpalii.diploma.data.service.PublicationService
import com.orestpalii.diploma.data.service.UserProfileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URL

enum class ProfilePostTab {
    CREATED, LIKED
}

open class ProfileViewModel(private val userId: String? = null) : ViewModel() {

    private val currentUserId = UserProfileService.currentUserId
    val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val _selectedTab = MutableStateFlow(ProfilePostTab.CREATED)
    val selectedTab: StateFlow<ProfilePostTab> = _selectedTab

    val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _subscriptionsCount = MutableStateFlow(0)
    val subscriptionsCount: StateFlow<Int> = _subscriptionsCount

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl

    private val _avatarBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val avatarBitmap: StateFlow<android.graphics.Bitmap?> = _avatarBitmap

    val isCurrentUser: Boolean = (userId == null || userId == currentUserId)

    fun setSelectedTab(tab: ProfilePostTab) {
        _selectedTab.value = tab
        fetchUserData()
    }

    fun fetchUserData() {
        val uid = userId ?: currentUserId ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val profile = UserProfileService.fetchUserProfile(uid)
                _userProfile.value = profile
                _userEmail.value = profile.email
                _avatarUrl.value = profile.avatarURL

                fetchAvatarBitmap(profile.avatarURL)

                _posts.value = when (_selectedTab.value) {
                    ProfilePostTab.CREATED -> PublicationService.fetchUserPosts(uid)
                    ProfilePostTab.LIKED -> if (isCurrentUser) PublicationService.fetchLikedPosts(uid) else emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchAvatarBitmap(urlString: String?) {
        if (urlString.isNullOrEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = URL(urlString).openStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                _avatarBitmap.value = bitmap
            } catch (_: Exception) {
            }
        }
    }

    fun fetchCounters() {
        val uid = userId ?: return
        viewModelScope.launch {
            _followersCount.value = UserProfileService.fetchFollowers(uid).size
            _subscriptionsCount.value = UserProfileService.fetchSubscriptions(uid).size
        }
    }

    fun checkSubscriptionStatus() {
        val target = userId ?: return
        val current = currentUserId ?: return
        if (target == current) return

        viewModelScope.launch {
            _isSubscribed.value = UserProfileService.isSubscribed(toUserId = target, fromUserId = current)
        }
    }

    fun toggleSubscription() {
        val target = userId ?: return
        val current = currentUserId ?: return
        if (target == current) return

        viewModelScope.launch {
            if (_isSubscribed.value) {
                UserProfileService.unsubscribe(fromUserId = current, toUserId = target)
                _isSubscribed.value = false
            } else {
                UserProfileService.subscribe(toUserId = target, fromUserId = current)
                _isSubscribed.value = true
            }
            fetchCounters()
        }
    }

    fun logout() {
        try {
            UserProfileService.logout()
            _isLoggedOut.value = true
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
        }
    }

    fun chunkTagsToRows(tags: List<String>, maxWidth: Float, charWidth: Float = 12f): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var currentWidth = 0f

        tags.forEach { tag ->
            val estimatedWidth = tag.length * charWidth + 28f
            if (currentWidth + estimatedWidth > maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf(tag)
                currentWidth = estimatedWidth + 10f
            } else {
                currentRow.add(tag)
                currentWidth += estimatedWidth + 10f
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        return rows
    }
}