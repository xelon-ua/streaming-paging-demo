package com.example.streaming_paging_demo.platform

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbarFor(
    listState: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(listState)
    )
}
