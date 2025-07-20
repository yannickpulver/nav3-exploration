package com.appswithlove.nav3_exploration.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appswithlove.nav3_exploration.ui.login.Login
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RouteViewModel(settings: ObservableSettings) : ViewModel() {

    @OptIn(ExperimentalSettingsApi::class)
    val state = settings.getBooleanFlow(Login.key, false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}