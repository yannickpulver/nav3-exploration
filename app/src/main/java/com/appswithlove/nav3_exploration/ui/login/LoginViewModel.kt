package com.appswithlove.nav3_exploration.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random


class LoginViewModel(private val settings: ObservableSettings) : ViewModel() {

    fun login() {
        settings.putBoolean(Login.key, true)
    }
}

object Login {
    val key = "login"
}