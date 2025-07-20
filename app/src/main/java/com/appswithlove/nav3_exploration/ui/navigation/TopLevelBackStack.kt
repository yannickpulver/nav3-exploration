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

    // Internal access for the saver
    internal fun getStartKey(): T = startKey
    internal fun getAllBackStacks(): Map<T, List<T>> = topLevelBackStacks.mapValues { it.value.toList() }
    internal fun restoreBackStacks(backStacks: Map<T, List<T>>) {
        topLevelBackStacks.clear()
        backStacks.forEach { (key, stack) ->
            topLevelBackStacks[key] = mutableStateListOf<T>().apply { addAll(stack) }
        }
    }

    internal fun updateBackStack() {
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
            startKey = serialize(backStack.getStartKey()),
            topLevelKey = serialize(backStack.topLevelKey),
            topLevelBackStacks = backStack.getAllBackStacks().mapKeys { serialize(it.key) }
                .mapValues { it.value.map { key -> serialize(key) } }
        )
        Json.encodeToString(state)
    },
    restore = { saved ->
        val state: TopLevelBackStackState = Json.decodeFromString(saved)
        val startKey = deserialize(state.startKey)
        val topLevelKey = deserialize(state.topLevelKey)

        TopLevelBackStack(startKey).apply {
            // Restore all backstacks
            val restoredBackStacks = state.topLevelBackStacks.mapKeys { deserialize(it.key) }
                .mapValues { it.value.map { key -> deserialize(key) } }
            restoreBackStacks(restoredBackStacks)
            
            // Set the current top level key - this will also call updateBackStack
            if (topLevelKey != startKey) {
                switchTopLevel(topLevelKey)
            } else {
                updateBackStack() // Still need to update if we're staying on start key
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
            is Screens.ProfileDetail -> "ProfileDetail:${navKey.id}"
            is Screens.HomeInfo -> "HomeInfo"
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
            serialized.startsWith("ProfileDetail:") -> {
                val id = serialized.substringAfter("ProfileDetail:").toInt()
                Screens.ProfileDetail(id)
            }
            serialized == "HomeInfo" -> Screens.HomeInfo
            else -> throw IllegalArgumentException("Unknown serialized NavKey: $serialized")
        }
    }
)