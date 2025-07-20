package com.appswithlove.nav3_exploration.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.SceneStrategy
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.appswithlove.nav3_exploration.ui.detail.DetailScreen
import com.appswithlove.nav3_exploration.ui.home.HomeScreen
import com.appswithlove.nav3_exploration.ui.navigation.BottomBar
import com.appswithlove.nav3_exploration.ui.navigation.Screens
import com.appswithlove.nav3_exploration.ui.navigation.TopLevelBackStack
import com.appswithlove.nav3_exploration.ui.navigation.rememberTopLevelBackStack
import com.appswithlove.nav3_exploration.ui.navigation.strategies.OverlaySceneStrategy
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.ListDetailSceneStrategy
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.rememberListDetailSceneStrategy
import com.appswithlove.nav3_exploration.ui.overlay.Overlay
import com.appswithlove.nav3_exploration.ui.profile.ProfileScreen
import kotlin.random.Random


@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalSharedTransitionScope. You must provide a " + "SharedTransitionScope from a call to SharedTransitionLayout()"
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ComposeApp() {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val topLevelBackStack = rememberTopLevelBackStack(Screens.Home)

            val overlaySceneStrategy = remember { OverlaySceneStrategy<Any>() }
            val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>()
            val sceneStrategy = remember { overlaySceneStrategy.then(listDetailSceneStrategy) }

            Navigation(topLevelBackStack, sceneStrategy)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun Navigation(
    topLevelBackStack: TopLevelBackStack<NavKey>,
    sceneStrategy: SceneStrategy<Any>
) {
    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { keysToRemove -> repeat(keysToRemove) { topLevelBackStack.removeLast() } },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Screens.Home>(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = "home",
                    bottomBar = {
                        BottomBar(
                            selected = topLevelBackStack.topLevelKey,
                            navigate = { topLevelBackStack.switchTopLevel(it) })
                    },
                    detailPlaceholder = {
                        PlaceholderPane()
                    }
                )) {
                HomeScreen(
                    openDetail = {
                        topLevelBackStack.add(Screens.HomeDetail(Random.nextInt()))
                    },
                    openDialog = { topLevelBackStack.add(Screens.Overlay) },
                )
            }

            entry<Screens.HomeDetail>(metadata = ListDetailSceneStrategy.detailPane(sceneKey = "home")) {
                DetailScreen(
                    "Detail",
                    back = {
                        // TODO: Check if we can somehow access the scenes calculateBack logic and use that here instead
                        while (topLevelBackStack.backStack.lastOrNull() is Screens.HomeDetail) {
                            topLevelBackStack.removeLast()
                        }
                    },
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }

            entry<Screens.Overlay>(metadata = OverlaySceneStrategy.overlay()) {
                Overlay(close = { topLevelBackStack.removeLast() })
            }

            entry<Screens.Profile>(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = "profile",
                    bottomBar = {
                        BottomBar(
                            selected = topLevelBackStack.topLevelKey,
                            navigate = { topLevelBackStack.switchTopLevel(it) })
                    }
                )) {
                ProfileScreen(
                    openDetail = {
                        topLevelBackStack.add(Screens.ProfileDetail(Random.nextInt()))
                    }
                )
            }

            entry<Screens.ProfileDetail> {
                DetailScreen(
                    "Detail",
                    back = {
                        // TODO: Check if we can somehow access the scenes calculateBack logic and use that here instead
                        while (topLevelBackStack.backStack.lastOrNull() is Screens.ProfileDetail) {
                            topLevelBackStack.removeLast()
                        }
                    },
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        },
        sceneStrategy = sceneStrategy,
        transitionSpec = { bouncy() },
        popTransitionSpec = { bouncy() })
}

private fun bouncy(): ContentTransform = ContentTransform(
    targetContentEnter = scaleIn(animationSpec = tween(150), initialScale = 0.8f) + fadeIn(
        animationSpec = tween(150)
    ),
    initialContentExit = scaleOut(animationSpec = tween(150), targetScale = 1.1f) + fadeOut(
        animationSpec = tween(150)
    )
)

fun fadeOnly() = NavDisplay.transitionSpec {
    ContentTransform(
        targetContentEnter = fadeIn(animationSpec = tween(200)),
        initialContentExit = fadeOut(animationSpec = tween(200))
    )
} + NavDisplay.popTransitionSpec {
    ContentTransform(
        targetContentEnter = fadeIn(animationSpec = tween(200)),
        initialContentExit = fadeOut(animationSpec = tween(200))
    )
}

@Composable
private fun PlaceholderPane(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Select an item",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap 'Open Detail' to view details here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


