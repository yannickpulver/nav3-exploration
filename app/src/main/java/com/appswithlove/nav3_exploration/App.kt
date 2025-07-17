package com.appswithlove.nav3_exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable


@Serializable
object Home : NavKey

@Serializable
object HomeDetail : NavKey

@Serializable
data object Profile : NavKey

val bottombarItems = listOf(Home, Profile)

@Composable
fun App() {
    val backStack = rememberNavBackStack(Home)

    LaunchedEffect(backStack.toList()) {
        println("Backstack changed: $backStack")
    }


    val sceneStrategy = remember {
        AdaptiveTwoPaneStrategy<Any>(
            bottomBar = {
                BottomBar(
                    selected = backStack.lastOrNull(),
                    navigate = {
                        if (backStack.lastOrNull() != it && backStack.lastOrNull() in bottombarItems) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        if (!backStack.contains(it)) {
                            backStack.add(it)
                        }
                    })
            }
        )
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Home>(metadata = mapOf(KEY_TWO_PANE to true, KEY_BOTTOM_BAR to true)) {
                Screen(
                    "Home",
                    openDetail = {
                        if (backStack.lastOrNull() == HomeDetail) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        backStack.add(HomeDetail)
                    },
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                )
            }

            entry<HomeDetail>(metadata = mapOf(KEY_TWO_PANE to true)) {
                DetailScreen(
                    "Detail",
                    back = { backStack.removeAt(backStack.lastIndex) },
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                )
            }

            entry<Profile>(metadata = mapOf(KEY_BOTTOM_BAR to true)) {
                Screen(
                    "Profile",
                    openDetail = {
                        if (backStack.lastOrNull() == HomeDetail) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        backStack.add(HomeDetail)
                    },
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer)
                )
            }
        },
        sceneStrategy = sceneStrategy
    )
}

@Composable
private fun Screen(title: String = "Home", openDetail: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title)
                Button(onClick = openDetail) {
                    Text("Open Detail")
                }
            }
        }
    }
}

@Composable
private fun DetailScreen(title: String = "Home", back: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title)
                OutlinedButton(onClick = back) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun BottomBar(selected: NavKey?, navigate: (NavKey) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        bottombarItems.forEach { destination ->

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null
                    )
                },
                selected = destination == selected,
                onClick = { navigate(destination) })
        }
    }
}
