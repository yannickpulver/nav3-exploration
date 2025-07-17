// Navigation3AdaptiveScenes.kt
// Full drop-in implementation for Navigation 3 (alpha-01)

package com.appswithlove.nav3_exploration

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


/* ------------------------------------------------------------------ */
/*  Metadata keys                                                     */
/* ------------------------------------------------------------------ */
const val KEY_TWO_PANE = "twoPane"      // true ⇢ destination wants to participate in a 2-pane scene
const val KEY_BOTTOM_BAR = "bottomBar"    // true ⇢ destination wants BottomBar to be shown

/* ------------------------------------------------------------------ */
/*  Single-pane scene with optional BottomBar                         */
/* ------------------------------------------------------------------ */
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
            AnimatedVisibility(entries.any { it.metadata.containsKey(KEY_BOTTOM_BAR) }) {
                bottomBar?.invoke()
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Two-pane scene with optional BottomBar                            */
/* ------------------------------------------------------------------ */
class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val first: NavEntry<T>,
    val second: NavEntry<T>,
    private val bottomBar: (@Composable () -> Unit)? = null
) : Scene<T> {

    override val entries = listOf(first, second)

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
            AnimatedVisibility(entries.any { it.metadata.containsKey(KEY_BOTTOM_BAR) }) {
                bottomBar?.invoke()
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Two-pane *or* single-pane strategy                                */
/*  – Shows BottomBar if *any* of the top entries asks for it         */
/*  – Creates a 2-pane scene only if                                 */
/*       • window width ≥ medium breakpoint AND                       */
/*       • BOTH top entries have KEY_TWO_PANE == true                 */
/* ------------------------------------------------------------------ */
class AdaptiveTwoPaneStrategy<T : Any>(
    private val bottomBar: @Composable () -> Unit,
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

        /* --- decide which scene shape we can support ---------------- */
        val canSplit = windowSize.isWidthAtLeastBreakpoint(minWidthBreakpoint) &&
                entries.size >= 2

        val lastTwo = if (canSplit) entries.takeLast(2) else emptyList()
        val bothTwoPane = lastTwo.all { it.metadata[KEY_TWO_PANE] == true }
        val showBottomBar =
            (entries.takeLast(if (canSplit) 2 else 1))
                .any { it.metadata[KEY_BOTTOM_BAR] == true }

        return when {
            /* 2-pane possible and requested by both destinations */
            canSplit && bothTwoPane -> {
                TwoPaneScene(
                    key = lastTwo.map { it.contentKey },
                    previousEntries = entries.dropLast(2),
                    first = lastTwo[0],
                    second = lastTwo[1],
                    bottomBar = if (showBottomBar) bottomBar else null
                )
            }

            /* fall back: single-pane with/without BottomBar           */
            showBottomBar -> SingleEntryScene(
                key = last.contentKey,
                previousEntries = entries.dropLast(1),
                entry = last,
                bottomBar = if (showBottomBar) bottomBar else null
            )

            else -> null
        }
    }
}
