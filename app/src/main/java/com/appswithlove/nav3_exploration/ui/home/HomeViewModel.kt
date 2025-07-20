package com.appswithlove.nav3_exploration.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appswithlove.nav3_exploration.ui.login.Login
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random

data class HomeState(
    val text: String = "home",
    val id: Int = Random.nextInt()
)


class HomeViewModel(private val settings: ObservableSettings) : ViewModel() {

    val homeState = HomeState()

    val state = flowOf(homeState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = homeState
        )


    fun logout() {
        settings.putBoolean(Login.key, false)
    }
}