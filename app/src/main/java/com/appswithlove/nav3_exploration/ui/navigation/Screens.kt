package com.appswithlove.nav3_exploration.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class ScreenWrapper(
    val type: String,
    val data: String
)


@Serializable
sealed interface Screens : NavKey {
    @Serializable
    object Home : Screens

    @Serializable
    data class HomeDetail(val id: Int) : Screens

    @Serializable
    data object HomeInfo : Screens

    @Serializable
    data class ProfileDetail(val id: Int) : Screens

    @Serializable
    data object Profile : Screens

    @Serializable
    data object Overlay : Screens


    fun serialize(): String {
        val wrapper = when (this) {
            is Home -> ScreenWrapper("Home", Json.encodeToString(this))
            is HomeDetail -> ScreenWrapper("HomeDetail", Json.encodeToString(this))
            is HomeInfo -> ScreenWrapper("HomeInfo", Json.encodeToString(this))
            is Overlay -> ScreenWrapper("Overlay", Json.encodeToString(this))
            is Profile -> ScreenWrapper("Profile", Json.encodeToString(this))
            is ProfileDetail -> ScreenWrapper("ProfileDetail", Json.encodeToString(this))
        }
        return Json.encodeToString(wrapper)
    }
    companion object {
        val bottombarItems = listOf(Home, Profile)
        
        fun deserialize(serialized: String): Screens {
            val wrapper = Json.decodeFromString<ScreenWrapper>(serialized)
            return when (wrapper.type) {
                "Home" -> Json.decodeFromString<Home>(wrapper.data)
                "HomeDetail" -> Json.decodeFromString<HomeDetail>(wrapper.data)
                "HomeInfo" -> Json.decodeFromString<HomeInfo>(wrapper.data)
                "Overlay" -> Json.decodeFromString<Overlay>(wrapper.data)
                "Profile" -> Json.decodeFromString<Profile>(wrapper.data)
                "ProfileDetail" -> Json.decodeFromString<ProfileDetail>(wrapper.data)
                else -> throw IllegalArgumentException("Unknown screen type: ${wrapper.type}")
            }
        }
    }
}
