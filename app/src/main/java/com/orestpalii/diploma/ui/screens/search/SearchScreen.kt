package com.orestpalii.diploma.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.orestpalii.diploma.data.model.Post
import com.orestpalii.diploma.ui.helper.PinterestGridWrapper

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onPostSelected: (Post) -> Unit
) {
    val posts by viewModel.posts
    val searchQuery by viewModel.searchQuery
    val isLoading by viewModel.isLoading
    val isSearching by viewModel.isSearching
    val allPostsLoaded by viewModel.allPostsLoaded

    LaunchedEffect(Unit) {
        viewModel.loadInitialPosts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Pinterest Grid, розміщений з відступом вниз і "під" по Z
        PinterestGridWrapper(
            posts = posts,
            paddingTop = 45,
            onPostClick = { onPostSelected(it) },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 35.dp)
                .zIndex(0f)
        )

        // Верхній шар — SearchBar + індикатори
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f) // Пошук поверх Grid
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onSearch = viewModel::performSearch,
                onClear = viewModel::clearSearch
            )

            if (isLoading && searchQuery.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Зачекайте, будь ласка, ми обробляємо ваш запит",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center)
                }
            } else if (!isLoading && posts.isEmpty()) {
                Text(
                    "Нажаль нам не вдалось завантажити нічого за вашим запитом",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // "Завантажити ще..." кнопка або прогрес індикатор — поверх грида
        if (!isSearching && !isLoading && !allPostsLoaded) {
            TextButton(
                onClick = { viewModel.loadMorePosts() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .zIndex(2f)
            ) {
                Text("Завантажити ще...", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (isLoading && posts.isNotEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .zIndex(2f)
            )
        }
    }
}




@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(45.dp)
            )
            .padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Текстове поле
            TextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    if (it.trim().isEmpty()) onClear()
                },
                placeholder = { Text("Пошуковий запит") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

            // Кнопка пошуку
            IconButton(
                onClick = onSearch,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(45.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Пошук",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Кнопка очищення
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(45.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистити",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}