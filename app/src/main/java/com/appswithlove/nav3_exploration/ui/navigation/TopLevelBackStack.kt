package com.appswithlove.nav3_exploration.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

class TopLevelBackStack<T : NavKey>(private val startKey: T) {

    private var topLevelBackStacks: HashMap<T, SnapshotStateList<T>> = hashMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf<T>(startKey)

    private fun updateBackStack() {
        backStack.clear()
        val currentStack = topLevelBackStacks[topLevelKey] ?: emptyList()

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