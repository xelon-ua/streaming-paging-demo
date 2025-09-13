package com.example.streaming_paging_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaming_paging_demo.orders.Order
import com.example.streaming_paging_demo.orders.OrderFilter
import com.example.streaming_paging_demo.orders.OrdersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import ua.wwind.paging.core.PagingData

class OrdersViewModel(
    private val repository: OrdersRepository = OrdersRepository(),
) : ViewModel() {

    private val _filter = MutableStateFlow(OrderFilter())
    val filter: StateFlow<OrderFilter> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val paging: StateFlow<PagingData<Order>?> =
        filter.flatMapLatest { repository.getPagingOrders(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    fun setFilter(filter: OrderFilter) {
        _filter.value = filter
    }
}
