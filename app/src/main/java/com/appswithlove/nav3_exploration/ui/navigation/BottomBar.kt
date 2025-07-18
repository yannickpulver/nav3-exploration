package com.appswithlove.nav3_exploration.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.appswithlove.nav3_exploration.ui.LocalSharedTransitionScope

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
            Screens.bottombarItems.forEach { destination ->
                NavigationBarItem(
                    icon = {
                        val iconVector = when (destination) {
                            Screens.Home -> Icons.Filled.Home
                            Screens.Profile -> Icons.Filled.Person
                            else -> Icons.Filled.Home
                        }
                        Icon(imageVector = iconVector, contentDescription = null)
                    },
                    selected = destination == selected,
                    onClick = { navigate(destination) },
                    label = {
                        val text = when (destination) {
                            Screens.Home -> "Home"
                            Screens.Profile -> "Profile"
                            else -> "Home"
                        }
                        Text(text = text)
                    })
            }
        }
    }
}