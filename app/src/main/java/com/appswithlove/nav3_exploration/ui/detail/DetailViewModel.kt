package com.appswithlove.nav3_exploration.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random

data class DetailState(
    val text: String = "home",
    val id: Int = Random.nextInt()
)


class DetailViewModel : ViewModel() {

    val detailState = DetailState()

    val state = flowOf(detailState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = detailState
        )

}