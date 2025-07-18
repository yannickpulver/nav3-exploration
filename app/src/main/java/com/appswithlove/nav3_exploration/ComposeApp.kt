package com.appswithlove.nav3_exploration

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.appswithlove.nav3_exploration.ui.detail.DetailScreen
import com.appswithlove.nav3_exploration.ui.home.HomeScreen
import com.appswithlove.nav3_exploration.ui.profile.ProfileScreen
import kotlinx.serialization.Serializable
import kotlin.random.Random


@Serializable
object Home : NavKey

@Serializable
data class HomeDetail(val id: Int) : NavKey

@Serializable
data object Profile : NavKey

@Serializable
data object Overlay : NavKey

val bottombarItems = listOf(Home, Profile)

@OptIn(ExperimentalSharedTransitionApi::class)
private val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalSharedTransitionScope. You must provide a " + "SharedTransitionScope from a call to SharedTransitionLayout()"
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ComposeApp() {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val backStack = rememberNavBackStack(Home)

            val sceneStrategy = remember {
                OverlaySceneStrategy<Any>().then(
                    AdaptiveTwoPaneStrategy<Any>(bottomBar = {
                        BottomBar(
                            selected = backStack.lastOrNull(), navigate = {
                                if (backStack.lastOrNull() != it && backStack.lastOrNull() in bottombarItems) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                if (!backStack.contains(it)) {
                                    backStack.add(it)
                                }
                            })
                    }, placeholder = {
                        PlaceholderPane()
                    })
                )
            }

            NavDisplay(
                backStack = backStack, entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ), entryProvider = entryProvider {
                    entry<Home>(metadata = AdaptiveTwoPaneStrategy.twoPane() + AdaptiveTwoPaneStrategy.bottomBar() + AdaptiveTwoPaneStrategy.placeholder() + fadeOnly()) {
                        HomeScreen(
                            openDetail = {
                                if (backStack.lastOrNull() is HomeDetail) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                backStack.add(HomeDetail(Random.nextInt()))
                            },
                            openDialog = { backStack.add(Overlay) },
                        )
                    }

                    entry<HomeDetail>(metadata = AdaptiveTwoPaneStrategy.twoPane()) {
                        DetailScreen(
                            "Detail",
                            back = { backStack.removeAt(backStack.lastIndex) },
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }

                    entry<Overlay>(metadata = OverlaySceneStrategy.overlay()) {
                        Column {
                            Text("Overlay")
                            Button(onClick = { backStack.removeAt(backStack.lastIndex) }) {
                                Text("Close")
                            }
                        }
                    }

                    entry<Profile>(metadata = AdaptiveTwoPaneStrategy.bottomBar() + fadeOnly()) {
                        ProfileScreen(
                            openDetail = {
                                if (backStack.lastOrNull() is HomeDetail) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                                backStack.add(HomeDetail(Random.nextInt()))
                            }
                        )
                    }
                }, sceneStrategy = sceneStrategy, transitionSpec = {
                    ContentTransform(
                        targetContentEnter = scaleIn(
                            animationSpec = tween(150), initialScale = 0.8f
                        ) + fadeIn(
                            animationSpec = tween(150)
                        ), initialContentExit = scaleOut(
                            animationSpec = tween(150), targetScale = 1.1f
                        ) + fadeOut(
                            animationSpec = tween(150)
                        )
                    )
                }, popTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = scaleIn(
                            animationSpec = tween(150), initialScale = 1.1f
                        ) + fadeIn(
                            animationSpec = tween(150)
                        ), initialContentExit = scaleOut(
                            animationSpec = tween(150), targetScale = 0.8f
                        ) + fadeOut(
                            animationSpec = tween(150)
                        )
                    )
                })
        }
    }
}

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
private fun Screen(
    title: String = "Home",
    openDetail: () -> Unit,
    openDialog: () -> Unit,
    color: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = color) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title)
                Button(onClick = openDetail) {
                    Text("Open Detail")
                }
                Button(onClick = openDialog) {
                    Text("Open Dialog")
                }
            }
        }
    }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BottomBar(selected: NavKey?, navigate: (NavKey) -> Unit, modifier: Modifier = Modifier) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        NavigationBar(
            modifier = modifier.sharedElement(
                rememberSharedContentState(key = "bottom_navigation"),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current
            )
        ) {
            bottombarItems.forEach { destination ->
                NavigationBarItem(icon = {
                    val iconVector = when (destination) {
                        Home -> Icons.Filled.Home
                        Profile -> Icons.Filled.Person
                        else -> Icons.Filled.Home
                    }
                    Icon(
                        imageVector = iconVector, contentDescription = null
                    )
                }, selected = destination == selected, onClick = { navigate(destination) })
            }
        }
    }
}
