package com.appswithlove.nav3_exploration.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.appswithlove.nav3_exploration.ui.navigation.strategies.AdaptiveTwoPaneStrategy
import com.appswithlove.nav3_exploration.ui.LocalSharedTransitionScope


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun sharedElementDecorator(): NavEntryDecorator<NavKey> = remember {
    navEntryDecorator<NavKey> { entry ->
        if (entry.metadata[AdaptiveTwoPaneStrategy.KEY_BOTTOM_BAR] != true) {
            entry.Content()
            return@navEntryDecorator
        }

        with(LocalSharedTransitionScope.current) {
            Box(
                Modifier.sharedElement(
                    rememberSharedContentState(entry.contentKey),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                ),
            ) {
                entry.Content()
            }
        }
    }
}