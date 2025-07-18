package com.appswithlove.nav3_exploration.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TopLevelBackStack<T : NavKey>(private val startKey: T) {

    private var topLevelBackStacks: HashMap<T, SnapshotStateList<T>> = hashMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf<T>(startKey)

    private fun updateBackStack() {
        println("Current backstack: $backStack")
        backStack.clear()
        val currentStack = topLevelBackStacks[topLevelKey] ?: emptyList()
        println("CurrentStack backstack: $currentStack")


        if (topLevelKey == startKey) {
            backStack.addAll(currentStack)
        } else {
            val startStack = topLevelBackStacks[startKey] ?: emptyList()
            backStack.addAll(startStack + currentStack)
        }
    }

    fun switchTopLevel(key: T) {
        if (topLevelBackStacks[key] == null) {
            topLevelBackStacks[key] = mutableStateListOf(key)
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {


        topLevelBackStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val currentStack = topLevelBackStacks[topLevelKey] ?: return

        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (topLevelKey != startKey) {
            topLevelKey = startKey
        }
        updateBackStack()
    }

    fun replaceStack(vararg keys: T) {
        topLevelBackStacks[topLevelKey] = mutableStateListOf(*keys)
        updateBackStack()
    }
}

@Composable
fun rememberTopLevelBackStack(startKey: NavKey): TopLevelBackStack<NavKey> {
    return rememberSaveable(saver = topLevelBackStackSaver()) { TopLevelBackStack(startKey) }
}

// Data class to represent the saveable state of TopLevelBackStack
@Serializable
private data class TopLevelBackStackState(
    val startKey: String,
    val topLevelKey: String,
    val topLevelBackStacks: Map<String, List<String>>
)

// Saver for TopLevelBackStack
fun <T : NavKey> topLevelBackStackSaver(
    serialize: (T) -> String,
    deserialize: (String) -> T
): Saver<TopLevelBackStack<T>, String> = Saver(
    save = { backStack ->
        val state = TopLevelBackStackState(
            startKey = serialize(backStack.backStack.first()), // The start key is always first
            topLevelKey = serialize(backStack.topLevelKey),
            topLevelBackStacks = mapOf(
                serialize(backStack.topLevelKey) to backStack.backStack.map { serialize(it) }
            )
        )
        Json.encodeToString(state)
    },
    restore = { saved ->
        val state: TopLevelBackStackState = Json.decodeFromString(saved)
        val startKey = deserialize(state.startKey)
        val topLevelKey = deserialize(state.topLevelKey)

        TopLevelBackStack(startKey).apply {
            if (topLevelKey != startKey) {
                switchTopLevel(topLevelKey)
            }

            val savedBackStack =
                state.topLevelBackStacks[state.topLevelKey]?.map { deserialize(it) }
            if (savedBackStack != null && savedBackStack.size > 1) {
                savedBackStack.drop(1).forEach { key ->
                    add(key)
                }
            }
        }
    }
)

// Helper function to create saver for Screens NavKey types
fun topLevelBackStackSaver(): Saver<TopLevelBackStack<NavKey>, String> = topLevelBackStackSaver(
    serialize = { navKey ->
        when (navKey) {
            is Screens.Home -> "Home"
            is Screens.Profile -> "Profile"
            is Screens.Overlay -> "Overlay"
            is Screens.HomeDetail -> "HomeDetail:${navKey.id}"
            else -> throw IllegalArgumentException("Unknown NavKey type: $navKey")
        }
    },
    deserialize = { serialized ->
        when {
            serialized == "Home" -> Screens.Home
            serialized == "Profile" -> Screens.Profile
            serialized == "Overlay" -> Screens.Overlay
            serialized.startsWith("HomeDetail:") -> {
                val id = serialized.substringAfter("HomeDetail:").toInt()
                Screens.HomeDetail(id)
            }

            else -> throw IllegalArgumentException("Unknown serialized NavKey: $serialized")
        }
    }
)