package com.example.streaming_paging_demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.streaming_paging_demo.component.FiltersBlock
import com.example.streaming_paging_demo.orders.Order
import com.example.streaming_paging_demo.orders.OrderStatus
import ua.wwind.paging.core.EntryState
import com.example.streaming_paging_demo.platform.VerticalScrollbarFor

@Composable
fun OrdersDemoScreen() {
    val vm: OrdersViewModel = viewModel()
    val pagingState = vm.paging.collectAsState()
    val currentFilter by vm.filter.collectAsState()

    Column(Modifier.fillMaxSize()) {
        // Filters block
        FiltersBlock(
            filter = currentFilter,
            onFilterChange = { vm.setFilter(it) },
        )
        HorizontalDivider()

        val orders = pagingState.value
        if (orders != null) {
            Text("Items: ${orders.data.size}")
            val listState = rememberLazyListState()
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(orders.data.size) { index ->
                        val entry = orders.data[index]
                        if (entry is EntryState.Success<Order>) {
                            OrderListItem(order = entry.value)
                        } else {
                            ListItem(
                                headlineContent = { Text("Loading…") },
                                supportingContent = { Text("Fetching order details") }
                            )
                        }
                        HorizontalDivider()
                    }
                }
                VerticalScrollbarFor(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun OrderListItem(order: Order) {
    val statusColor = when (order.status) {
        OrderStatus.PAID, OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    ListItem(
        leadingContent = {
            val initial: String = order.customer
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.take(1)
                ?.uppercase()
                ?: "?"
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initial)
            }
        },
        headlineContent = {
            Text(
                text = "#${order.id} • ${order.customer}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row {
                Text(
                    text = order.deliveryAddress
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("•")
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.status.toString(),
                    color = statusColor
                )
            }
        },
        trailingContent = {
            Text(formatAmount(order.amount))
        }
    )
}

private fun formatAmount(amount: Any?): String {
    return when (amount) {
        is Int -> "$$amount"
        is Long -> "$$amount"
        is Float -> "$" + formatDecimal2(amount.toDouble())
        is Double -> "$" + formatDecimal2(amount)
        is String -> amount
        else -> amount?.toString() ?: "—"
    }
}

private fun formatDecimal2(value: Double): String {
    val scaled = kotlin.math.round(value * 100.0) / 100.0
    val text = scaled.toString()
    return if (text.contains('.')) {
        val parts = text.split('.')
        val decimals = parts[1].padEnd(2, '0').take(2)
        parts[0] + "." + decimals
    } else {
        "$text.00"
    }
}


