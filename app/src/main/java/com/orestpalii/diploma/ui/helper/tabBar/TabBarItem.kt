package com.orestpalii.diploma.ui.helper.tabBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orestpalii.diploma.data.model.Tab
import com.orestpalii.diploma.ui.theme.DiplomaTheme

@Composable
fun TabBarItem(tab: Tab, selectedTab: Tab, onClick: () -> Unit) {
    val icon = when (tab) {
        Tab.HOME -> Icons.Default.Search
        Tab.CREATE -> Icons.Default.Add
        Tab.PROFILE -> Icons.Default.Person
        Tab.RECOMMENDED -> Icons.Default.Home
    }

    val isSelected = tab == selectedTab

    Surface(
        modifier = Modifier
            .padding(6.dp)
            .size(52.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = tab.name, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabBarItemPreviewSelected() {
    DiplomaTheme {
        TabBarItem(
            tab = Tab.HOME,
            selectedTab = Tab.HOME,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TabBarItemPreviewUnselected() {
    DiplomaTheme {
        TabBarItem(
            tab = Tab.HOME,
            selectedTab = Tab.PROFILE,
            onClick = {}
        )
    }
}
