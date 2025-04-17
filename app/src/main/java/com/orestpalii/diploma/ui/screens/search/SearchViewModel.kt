package com.orestpalii.diploma.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.service.PublicationService
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val pageSize = 10
    private var currentPage = 0
    private var allPosts: List<Post> = emptyList()

    var posts = mutableStateOf<List<Post>>(emptyList())
        private set
    var searchQuery = mutableStateOf("")
        private set
    var isSearching = mutableStateOf(false)
        private set
    var isLoading = mutableStateOf(false)
        private set
    var allPostsLoaded = mutableStateOf(false)
        private set

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun loadInitialPosts() {
        viewModelScope.launch {
            if (isLoading.value) return@launch
            isLoading.value = true
            try {
                allPosts = PublicationService.fetchAllPostsSortedByDate()
                posts.value = allPosts.take(pageSize)
                currentPage = 1
                allPostsLoaded.value = posts.value.size == allPosts.size
            } catch (e: Exception) {
                println("Error loading posts: ${e.localizedMessage}")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadMorePosts() {
        viewModelScope.launch {
            if (isLoading.value || allPostsLoaded.value) return@launch
            isLoading.value = true
            try {
                val start = currentPage * pageSize
                val end = minOf(start + pageSize, allPosts.size)
                val newPosts = allPosts.slice(start until end)
                posts.value = posts.value + newPosts
                currentPage++
                allPostsLoaded.value = posts.value.size == allPosts.size
            } finally {
                isLoading.value = false
            }
        }
    }

    fun performSearch() {
        viewModelScope.launch {
            if (searchQuery.value.trim().isEmpty()) return@launch
            isSearching.value = true
            isLoading.value = true
            try {
                val result = PublicationService.fetchSimilarPostsByText(searchQuery.value)
                posts.value = result
            } finally {
                isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        isSearching.value = false
        posts.value = emptyList()
        loadInitialPosts()
    }
}
