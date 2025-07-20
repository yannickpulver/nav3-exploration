package com.appswithlove.nav3_exploration.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


object Screens {
    @Serializable
    object Home : NavKey

    @Serializable
    data class HomeDetail(val id: Int) : NavKey

    @Serializable
    data object HomeInfo : NavKey

    @Serializable
    data class ProfileDetail(val id: Int) : NavKey

    @Serializable
    data object Profile : NavKey

    @Serializable
    data object Overlay : NavKey

    val bottombarItems = listOf(Screens.Home, Screens.Profile)

}
