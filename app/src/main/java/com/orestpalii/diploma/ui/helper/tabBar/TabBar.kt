package com.orestpalii.diploma.ui.helper.tabBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orestpalii.diploma.data.model.Tab

@Composable
fun TabBar(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 30.dp,
                topEnd = 30.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp))
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabBarItem(tab = Tab.RECOMMENDED, selectedTab = selectedTab) { onTabSelected(Tab.RECOMMENDED) }
        TabBarItem(tab = Tab.HOME, selectedTab = selectedTab) { onTabSelected(Tab.HOME) }
        TabBarItem(tab = Tab.CREATE, selectedTab = selectedTab) { onTabSelected(Tab.CREATE) }
        TabBarItem(tab = Tab.PROFILE, selectedTab = selectedTab) { onTabSelected(Tab.PROFILE) }
    }
}