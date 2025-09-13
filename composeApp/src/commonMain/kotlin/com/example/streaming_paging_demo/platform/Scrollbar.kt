package com.example.streaming_paging_demo.platform

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbarFor(
    listState: LazyListState,
    modifier: Modifier = Modifier
)
