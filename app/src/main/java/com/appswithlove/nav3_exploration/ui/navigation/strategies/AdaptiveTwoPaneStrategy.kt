// Navigation3AdaptiveScenes.kt
// Full drop-in implementation for Navigation 3 (alpha-01)

package com.appswithlove.nav3_exploration.ui.navigation.strategies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

class SingleEntryScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val bottomBar: (@Composable () -> Unit)? = null
) : Scene<T> {

    override val entries = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                entries.forEach {
                    Column(modifier = Modifier.weight(0.5f)) {
                        it.Content()
                    }
                }
            }
            AnimatedVisibility(entries.any { it.metadata.containsKey(AdaptiveTwoPaneStrategy.Companion.KEY_BOTTOM_BAR) }) {
                bottomBar?.invoke()
            }
        }
    }
}

class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val first: NavEntry<T>,
    val second: NavEntry<T>?,
    private val bottomBar: (@Composable () -> Unit)? = null,
    private val placeholder: (@Composable () -> Unit)? = null
) : Scene<T> {

    override val entries = listOfNotNull(first, second)

    override val content: @Composable (() -> Unit) = {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // First pane
                Column(modifier = Modifier.weight(0.5f)) {
                    first.Content()
                }

                // Second pane - either the actual entry or placeholder
                Column(modifier = Modifier.weight(0.5f)) {
                    if (second != null) {
                        second.Content()
                    } else {
                        placeholder?.invoke()
                    }
                }
            }
            AnimatedVisibility(entries.any { it.metadata.containsKey(AdaptiveTwoPaneStrategy.Companion.KEY_BOTTOM_BAR) }) {
                bottomBar?.invoke()
            }
        }
    }
}

class AdaptiveTwoPaneStrategy<T : Any>(
    private val bottomBar: @Composable () -> Unit,
    private val placeholder: (@Composable () -> Unit)? = null,
    private val minWidthBreakpoint: Int = WIDTH_DP_MEDIUM_LOWER_BOUND
) : SceneStrategy<T> {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit
    ): Scene<T>? {
        if (entries.isEmpty()) return null
        val last = entries.last()

        val windowSize = currentWindowAdaptiveInfo().windowSizeClass

        val canSplit = windowSize.isWidthAtLeastBreakpoint(minWidthBreakpoint)

        val lastTwo = if (canSplit && entries.size >= 2) entries.takeLast(2) else emptyList()
        val bothTwoPane = lastTwo.all { it.metadata[KEY_TWO_PANE] == true }
        val lastEntryTwoPane = last.metadata[KEY_TWO_PANE] == true
        val lastEntryWantsPlaceholder = last.metadata[KEY_PLACEHOLDER] == true

        val showBottomBar =
            (entries.takeLast(if (canSplit && entries.size >= 2) 2 else 1))
                .any { it.metadata[KEY_BOTTOM_BAR] == true }

        return when {
            /* 2-pane possible and requested by both destinations */
            canSplit && lastTwo.size == 2 && bothTwoPane -> {
                TwoPaneScene(
                    key = lastTwo.map { it.contentKey },
                    previousEntries = entries.dropLast(2),
                    first = lastTwo[0],
                    second = lastTwo[1],
                    bottomBar = if (showBottomBar) bottomBar else null,
                )
            }

            /* 2-pane possible with single entry + placeholder */
            canSplit && lastEntryTwoPane && lastEntryWantsPlaceholder && placeholder != null -> {
                TwoPaneScene(
                    key = last.contentKey,
                    previousEntries = entries.dropLast(1),
                    first = last,
                    second = null,
                    bottomBar = if (showBottomBar) bottomBar else null,
                    placeholder = placeholder
                )
            }

            /* fall back: single-pane with/without BottomBar           */
            showBottomBar -> SingleEntryScene(
                key = last.contentKey,
                previousEntries = entries.dropLast(1),
                entry = last,
                bottomBar = bottomBar
            )

            else -> null
        }
    }

    companion object {
        const val KEY_TWO_PANE = "twoPane"
        const val KEY_BOTTOM_BAR = "bottomBar"
        const val KEY_PLACEHOLDER = "placeholder"

        fun placeholder(): Map<String, Any> = mapOf(KEY_PLACEHOLDER to true)
        fun bottomBar(): Map<String, Any> = mapOf(KEY_BOTTOM_BAR to true)
        fun twoPane(): Map<String, Any> = mapOf(KEY_TWO_PANE to true)

    }
}
