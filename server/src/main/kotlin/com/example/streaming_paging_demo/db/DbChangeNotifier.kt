package com.example.streaming_paging_demo.db

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Global change notifier for DB mutations. Each mutation should call [bump] to
 * increment the version. Consumers can collect [version] to reactively re-read data.
 */
object DbChangeNotifier {
    private val _version: MutableStateFlow<Long> = MutableStateFlow(0L)
    val version: StateFlow<Long> = _version.asStateFlow()

    fun bump() {
        _version.update { current -> current + 1L }
    }
}
