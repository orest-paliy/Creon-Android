package com.orestpalii.diploma.ui.screens.userProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.ui.helper.PinterestGridWrapper

@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel = viewModel(
        key = userId ?: "",
        factory = ProfileViewModelFactory(userId)
    ),
    onLogout: () -> Unit,
    onPostSelected: (Post) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val userEmail by viewModel.userEmail.collectAsState()
    val avatarBitmap by viewModel.avatarBitmap.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val subscriptionsCount by viewModel.subscriptionsCount.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCurrentUser = viewModel.isCurrentUser

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
        viewModel.fetchCounters()
        viewModel.checkSubscriptionStatus()
    }

    Column(modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier
            .fillMaxWidth()) {
            if (!isCurrentUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    IconButton(onClick = {
                        if (onBack != null) {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        avatarBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        } ?: Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(20.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(userEmail, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoColumn("Публікацій", posts.size)
                                InfoColumn("Підписників", followersCount)
                                InfoColumn("Підписок", subscriptionsCount)
                            }
                        }
                    }
                    if (isCurrentUser) {
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Text("Вийти з акаунту", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.toggleSubscription() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isSubscribed) "Відписатись" else "Підписатись")
                        }
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Інтереси користувача", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))

            userProfile?.interests?.let { tags ->
                FlowRow(
                    mainAxisSpacing = 10.dp,
                    crossAxisSpacing = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    tags.forEach { tag ->
                        Text(
                            tag,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                FilterButton("Створені", selectedTab == ProfilePostTab.CREATED) {
                    viewModel.setSelectedTab(ProfilePostTab.CREATED)
                }
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(12.dp))
                    FilterButton("Вподобані", selectedTab == ProfilePostTab.LIKED) {
                        viewModel.setSelectedTab(ProfilePostTab.LIKED)
                    }
                }
            }

            if (isLoading && posts.isEmpty()) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Зачекайте, дані користувача завантажуються...", color = MaterialTheme.colorScheme.primary)
                }
            } else if (posts.isEmpty()) {
                Text("Немає публікацій", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                PinterestGridWrapper(
                    posts = posts, onPostClick = onPostSelected,
                    paddingTop = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Вийти з акаунту?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    onLogout()
                    showLogoutDialog = false
                }) {
                    Text("Вийти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}


@Composable
fun InfoColumn(title: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
        Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background
        )
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary)
    }
}

