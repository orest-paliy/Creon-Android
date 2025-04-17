package com.orestpalii.diploma.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.data.model.Tab
import com.orestpalii.diploma.ui.helper.ConfirmEmailScreen
import com.orestpalii.diploma.ui.helper.SetStatusBarColor
import com.orestpalii.diploma.ui.helper.tabBar.TabBar
import com.orestpalii.diploma.ui.screens.auth.AuthScreen
import com.orestpalii.diploma.ui.screens.auth.AuthViewModel
import com.orestpalii.diploma.ui.screens.createpost.CreatePostScreen
import com.orestpalii.diploma.ui.screens.interestOnboarding.InterestOnboardingScreen
import com.orestpalii.diploma.ui.screens.postDetail.PostDetailScreen
import com.orestpalii.diploma.ui.screens.recomendation.RecommendedPostsScreen
import com.orestpalii.diploma.ui.screens.search.SearchScreen
import com.orestpalii.diploma.ui.screens.userProfile.ProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    SetStatusBarColor(color = MaterialTheme.colorScheme.background, useDarkIcons = false)

    val authVm: AuthViewModel = viewModel()
    val isEmailSent     by remember { derivedStateOf { authVm.emailVerificationSent } }
    val isEmailVerified by remember { derivedStateOf { authVm.isEmailVerified } }
    val hasProfile      by remember { derivedStateOf { authVm.hasProfile } }

    when {
        // 1) Ще не надсилали лист — екран реєстрації/входу
        !isEmailSent && !isEmailVerified -> {
            AuthScreen(vm = authVm)
        }
        // 2) Лист надіслано, чекаємо натискання «Я підтвердив»
        isEmailSent && !isEmailVerified -> {
            ConfirmEmailScreen(
                onCheckAgain   = { authVm.checkEmailVerification() },
                onBackToAuth   = { authVm.resetEmailSent() }
            )
        }
        // 3) Пошта підтверджена, але профілю немає — онбординг
        isEmailVerified && !hasProfile -> {
            InterestOnboardingScreen(
                onFinish = { authVm.markProfileCreated() }
            )
        }
        // 4) Всі кроки пройдені — показуємо основний UI
        else -> {
            MainAppContent()
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MainAppContent() {
    val auth = FirebaseAuth.getInstance()
    val authVm: AuthViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(Tab.RECOMMENDED) }
    var showCreatePost by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var selectedAuthorId by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (selectedTab) {
            Tab.RECOMMENDED -> RecommendedPostsScreen(
                selectedPost = { post -> selectedPost = post }
            )
            Tab.HOME        -> SearchScreen       { post -> selectedPost = post }
            Tab.PROFILE     -> ProfileScreen(
                userId      = auth.currentUser?.uid,
                onLogout    = { authVm.logout() },
                onPostSelected = { post -> selectedPost = post },
                onBack      = null
            )
            Tab.CREATE      -> showCreatePost = true
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            TabBar(selectedTab = selectedTab) { tab ->
                selectedTab = tab
            }
        }

        if (showCreatePost) {
            CreatePostScreen(
                authorId        = auth.currentUser?.uid.orEmpty(),
                onFinishPosting = {
                    showCreatePost = false
                    selectedTab    = Tab.HOME
                }
            )
        }

        selectedPost?.let { post ->
            PostDetailScreen(
                postId         = post.id,
                onPostSelected = { selectedPost = it },
                onShowProfile  = { authorId ->
                    selectedPost     = null
                    selectedAuthorId = authorId
                },
                onBack         = { selectedPost = null }
            )
        }

        selectedAuthorId?.let { authorId ->
            ProfileScreen(
                userId         = authorId,
                onLogout       = { auth.signOut() },
                onPostSelected = { selectedPost = it },
                onBack         = { selectedAuthorId = null }
            )
        }
    }
}
