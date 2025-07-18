/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appswithlove.nav3_exploration.ui.navigation.strategies

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.OverlayScene
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/** An [androidx.navigation3.ui.OverlayScene] that renders an [entry] within a [Dialog] or [ModalBottomSheet]. */
internal class OverlayScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val dialogProperties: DialogProperties,
    private val isTablet: Boolean,
    private val onBack: (count: Int) -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    @OptIn(ExperimentalMaterial3Api::class)
    override val content: @Composable (() -> Unit) = {
        if (isTablet) {
            Dialog(onDismissRequest = { onBack(1) }, properties = dialogProperties) { 
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Surface(
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        entry.Content()
                    }
                }
            }
        } else {
            ModalBottomSheet(onDismissRequest = { onBack(1) }) {
                entry.Content()
            }
        }
    }
}

/**
 * A [SceneStrategy] that displays entries that have added [overlay] to their [NavEntry.metadata]
 * within a [Dialog] instance on tablets or a [ModalBottomSheet] on mobile devices.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
public class OverlaySceneStrategy<T : Any>(private val globalOnBack: (Int) -> Unit) : SceneStrategy<T> {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    public override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (count: Int) -> Unit,
    ): Scene<T>? {
        val lastEntry = entries.lastOrNull()
        val dialogProperties = lastEntry?.metadata?.get(DIALOG_KEY) as? DialogProperties
        return dialogProperties?.let { properties ->
            val windowSize = currentWindowAdaptiveInfo().windowSizeClass
            val isTablet = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
            
            OverlayScene(
                key = lastEntry.contentKey,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                dialogProperties = properties,
                isTablet = isTablet,
                onBack = globalOnBack, // required as otherwise it doesn't propagate the state to our toplevel backstack
            )
        }
    }

    public companion object {
        /**
         * Function to be called on the [NavEntry.metadata] to mark this entry as something that
         * should be displayed as an overlay. On tablets, this will be displayed within a [Dialog].
         * On mobile devices, this will be displayed within a [ModalBottomSheet].
         *
         * @param dialogProperties properties that should be passed to the containing [Dialog] when on tablets.
         */
        public fun overlay(
            dialogProperties: DialogProperties = DialogProperties()
        ): Map<String, Any> = mapOf(DIALOG_KEY to dialogProperties)

        internal const val DIALOG_KEY = "dialog"
    }
}
