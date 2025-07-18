package com.appswithlove.nav3_exploration.ui.overlay

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Overlay(close: () -> Unit) {
    Column {
        Text("Overlay")
        Button(onClick = close) {
            Text("Close")
        }
    }
}