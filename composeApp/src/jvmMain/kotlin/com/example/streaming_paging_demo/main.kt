package com.example.streaming_paging_demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "streaming_paging_demo",
    ) {
        App()
    }
}