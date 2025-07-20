package com.appswithlove.nav3_exploration.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.appswithlove.nav3_exploration.ui.home.PlaceholderPane
import com.appswithlove.nav3_exploration.ui.home.info.HomeInfoScreen
import com.appswithlove.nav3_exploration.ui.home.info.HomeMoreInfoScreen
import com.appswithlove.nav3_exploration.ui.loading.LoadingScreen
import com.appswithlove.nav3_exploration.ui.login.LoginScreen
import com.appswithlove.nav3_exploration.ui.navigation.strategies.OverlaySceneStrategy
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.ListDetailSceneStrategy
import com.appswithlove.nav3_exploration.ui.navigation.strategies.listdetail.rememberListDetailSceneStrategy
import com.appswithlove.nav3_exploration.ui.overlay.Overlay
import com.appswithlove.nav3_exploration.ui.profile.ProfileScreen
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalSharedTransitionScope. You must provide a " + "SharedTransitionScope from a call to SharedTransitionLayout()"
        )
    }

@Composable
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
fun AppNavigation(viewModel: RouteViewModel = koinViewModel()) {
    val isLoggedIn by viewModel.state.collectAsStateWithLifecycle()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val topLevelBackStack =
                rememberTopLevelBackStack(
                    when (isLoggedIn) {
                        true -> Screens.Home
                        false -> Screens.Login
                        else -> Screens.Loading
                    }
                )

            val overlaySceneStrategy = remember { OverlaySceneStrategy<Any>() }
            val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>()
            val sceneStrategy = remember { overlaySceneStrategy.then(listDetailSceneStrategy) }

            Navigation(topLevelBackStack, sceneStrategy)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun Navigation(
    topLevelBackStack: TopLevelBackStack<NavKey>,
    sceneStrategy: SceneStrategy<Any>,
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
            entry<Screens.Loading> {
                LoadingScreen()
            }

            entry<Screens.Login> {
                LoginScreen()
            }

            entry<Screens.Home>(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = "home",
                    bottomBar = {
                        BottomBar(
                            selected = topLevelBackStack.topLevelKey,
                            navigate = { topLevelBackStack.switchTopLevel(it) })
                    },
                    header = {
                        TopAppBar(
                            title = { Text("Home") },
                        )
                    },
                    detailPlaceholder = { PlaceholderPane() }
                )) {
                HomeScreen(
                    openDetail = { topLevelBackStack.add(Screens.HomeDetail(Random.nextInt())) },
                    openDialog = { topLevelBackStack.add(Screens.Overlay) },
                )
            }

            entry<Screens.HomeDetail>(metadata = ListDetailSceneStrategy.detailPane(sceneKey = "home")) {
                DetailScreen(
                    "Home Detail",
                    back = {
                        // TODO: Check if we can somehow access the scenes calculateBack logic and use that here instead
                        while (topLevelBackStack.backStack.any { it is Screens.HomeDetail }) {
                            topLevelBackStack.removeLast()
                        }
                    },
                    openInfo = { topLevelBackStack.add(Screens.HomeInfo) },
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }

            entry<Screens.HomeInfo>(metadata = ListDetailSceneStrategy.extraPane(sceneKey = "home")) {
                HomeInfoScreen(
                    back = { topLevelBackStack.removeLast() },
                    openMoreInfo = { topLevelBackStack.add(Screens.HomeMoreInfo) },
                )
            }

            entry<Screens.HomeMoreInfo>(metadata = ListDetailSceneStrategy.extraPane(sceneKey = "home")) {
                HomeMoreInfoScreen(
                    back = { topLevelBackStack.removeLast() },
                )
            }

            entry<Screens.Overlay>(metadata = OverlaySceneStrategy.overlay()) {
                Overlay(close = { topLevelBackStack.removeLast() })
            }

            entry<Screens.Profile>(
                // here we're currently just using listPane to display the bottomBar. Unsure if we should put that to a seperate Strategry
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = "profile",
                    bottomBar = {
                        BottomBar(
                            selected = topLevelBackStack.topLevelKey,
                            navigate = { topLevelBackStack.switchTopLevel(it) })
                    }
                )) {
                ProfileScreen(
                    openDetail = { topLevelBackStack.add(Screens.ProfileDetail(Random.nextInt())) }
                )
            }

            entry<Screens.ProfileDetail> {
                DetailScreen(
                    "Profile Detail",
                    back = { topLevelBackStack.removeLast() },
                    openInfo = { },
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


