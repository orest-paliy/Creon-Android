package com.orestpalii.diploma.ui.helper.tabBar

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.orestpalii.diploma.data.model.Tab

class TabBarViewModel : ViewModel() {
    var selectedTab = mutableStateOf(Tab.RECOMMENDED)
        private set

    fun updateTab(tab: Tab) {
        selectedTab.value = tab
    }
}
