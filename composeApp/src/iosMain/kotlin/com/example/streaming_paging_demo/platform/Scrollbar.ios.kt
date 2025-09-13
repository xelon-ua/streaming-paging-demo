package com.example.streaming_paging_demo.platform

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbarFor(
    listState: LazyListState,
    modifier: Modifier
) {
    // No-op on iOS
}
