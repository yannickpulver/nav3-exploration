package com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail

import androidx.collection.mutableIntListOf
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.Scene
import androidx.navigation3.ui.SceneStrategy
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.ListDetailSceneStrategy.Companion.detailPane
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.ListDetailSceneStrategy.Companion.extraPane
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.ListDetailSceneStrategy.Companion.listPane

/**
 * Creates and remembers a [ListDetailSceneStrategy].
 *
 * @param backNavigationBehavior the behavior describing which backstack entries may be skipped
 *   during the back navigation. See [BackNavigationBehavior].
 * @param directive The top-level directives about how the list-detail scaffold should arrange its
 *   panes.
 */
@ExperimentalMaterial3AdaptiveApi
@Composable
public fun <T : Any> rememberListDetailSceneStrategy(
    backNavigationBehavior: BackNavigationBehavior =
        BackNavigationBehavior.PopUntilCurrentDestinationChange,
    directive: PaneScaffoldDirective = customGaplessDirective(),
): ListDetailSceneStrategy<T> {
    return remember(backNavigationBehavior, directive) {
        ListDetailSceneStrategy(
            backNavigationBehavior = backNavigationBehavior,
            directive = directive,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun customGaplessDirective(): PaneScaffoldDirective {
    val systemDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val customDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = systemDirective.maxHorizontalPartitions,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = systemDirective.maxVerticalPartitions,
        verticalPartitionSpacerSize = systemDirective.verticalPartitionSpacerSize,
        defaultPanePreferredWidth = systemDirective.defaultPanePreferredWidth,
        excludedBounds = systemDirective.excludedBounds,
    )
    return customDirective
}

/**
 * A [ListDetailSceneStrategy] supports arranging [NavEntry]s into an adaptive
 * [ListDetailPaneScaffold]. By using [listPane], [detailPane], or [extraPane] in a NavEntry's
 * metadata, entries can be assigned as belonging to a list pane, detail pane, or extra pane. These
 * panes will be displayed together if the window size is sufficiently large, and will automatically
 * adapt if the window size changes, for example, on a foldable device.
 *
 * @param backNavigationBehavior the behavior describing which backstack entries may be skipped
 *   during the back navigation. See [BackNavigationBehavior].
 * @param directive The top-level directives about how the list-detail scaffold should arrange its
 *   panes.
 */
@ExperimentalMaterial3AdaptiveApi
public class ListDetailSceneStrategy<T : Any>(
    public val backNavigationBehavior: BackNavigationBehavior,
    public val directive: PaneScaffoldDirective,
) : SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (count: Int) -> Unit,
    ): Scene<T>? {
        if (!entries.last().isListDetailEntry()) return null
        val sceneKey = (entries.last().metadata[ListDetailRoleKey] as PaneMetadata).sceneKey
        val scaffoldEntries = mutableListOf<NavEntry<T>>()
        val scaffoldEntryIndices = mutableIntListOf()
        var detailPlaceholder: (@Composable ThreePaneScaffoldScope.() -> Unit)? = null
        var bottomBar: (@Composable () -> Unit)? = null
        for ((index, entry) in entries.withIndex()) {
            val paneMetadata = entry.metadata[ListDetailRoleKey] as? PaneMetadata
            if (paneMetadata != null && paneMetadata.sceneKey == sceneKey) {
                scaffoldEntryIndices.add(index)
                scaffoldEntries.add(entry)
                if (paneMetadata is ListMetadata) {
                    detailPlaceholder = paneMetadata.detailPlaceholder
                    bottomBar = paneMetadata.bottomBar
                }
            }
        }
        if (scaffoldEntries.isEmpty()) return null
        val scene =
            ListDetailScene(
                key = sceneKey,
                onBack = onBack,
                backNavBehavior = backNavigationBehavior,
                directive = directive,
                allEntries = entries,
                scaffoldEntries = scaffoldEntries,
                scaffoldEntryIndices = scaffoldEntryIndices,
                detailPlaceholder = detailPlaceholder ?: {},
                bottomBar = bottomBar ?: {},
            )

        if (scene.currentScaffoldValue.paneCount < 1) {
            return null
        }
        return scene
    }

    internal sealed interface PaneMetadata {
        val sceneKey: Any
    }

    internal class ListMetadata(
        override val sceneKey: Any,
        val detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit,
        val bottomBar: @Composable () -> Unit = {},
    ) : PaneMetadata

    internal class DetailMetadata(override val sceneKey: Any) : PaneMetadata
    internal class ExtraMetadata(override val sceneKey: Any) : PaneMetadata
    public companion object {
        internal val ListDetailRoleKey: String = ListDetailPaneScaffoldRole::class.qualifiedName!!

        /**
         * Constructs metadata to mark a [NavEntry] as belonging to a
         * [list pane][ListDetailPaneScaffoldRole.List] within a [ListDetailPaneScaffold].
         *
         * @param sceneKey the key to distinguish the scene of the list-detail scaffold, in case
         *   multiple list-detail scaffolds are supported within the same NavDisplay.
         * @param detailPlaceholder composable content to display in the detail pane in case there
         *   is no other [NavEntry] representing a detail pane in the backstack. Note that this
         *   content does not receive the same scoping mechanisms as a full-fledged [NavEntry].
         */
        public fun listPane(
            sceneKey: Any = Unit,
            detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit = {},
            bottomBar: @Composable () -> Unit = {},
        ): Map<String, Any> = mapOf(ListDetailRoleKey to ListMetadata(sceneKey, detailPlaceholder, bottomBar))

        /**
         * Constructs metadata to mark a [NavEntry] as belonging to a
         * [detail pane][ListDetailPaneScaffoldRole.Detail] within a [ListDetailPaneScaffold].
         *
         * @param sceneKey the key to distinguish the scene of the list-detail scaffold, in case
         *   multiple list-detail scaffolds are supported within the same NavDisplay.
         */
        public fun detailPane(sceneKey: Any = Unit): Map<String, Any> =
            mapOf(ListDetailRoleKey to DetailMetadata(sceneKey))

        /**
         * Constructs metadata to mark a [NavEntry] as belonging to an
         * [extra pane][ListDetailPaneScaffoldRole.Extra] within a [ListDetailPaneScaffold].
         *
         * @param sceneKey the key to distinguish the scene of the list-detail scaffold, in case
         *   multiple list-detail scaffolds are supported within the same NavDisplay.
         */
        public fun extraPane(sceneKey: Any = Unit): Map<String, Any> =
            mapOf(ListDetailRoleKey to ExtraMetadata(sceneKey))
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T : Any> NavEntry<T>.isListDetailEntry(): Boolean =
    metadata[ListDetailSceneStrategy.ListDetailRoleKey] != null

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val ThreePaneScaffoldValue.paneCount: Int
    get() {
        var count = 0
        if (this.primary != PaneAdaptedValue.Hidden) count++
        if (this.secondary != PaneAdaptedValue.Hidden) count++
        if (this.tertiary != PaneAdaptedValue.Hidden) count++
        return count
    }