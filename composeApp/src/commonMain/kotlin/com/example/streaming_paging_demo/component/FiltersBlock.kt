package com.example.streaming_paging_demo.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.streaming_paging_demo.orders.OrderFilter
import com.example.streaming_paging_demo.orders.OrderStatus

@Composable
internal fun FiltersBlock(
    filter: OrderFilter,
    onFilterChange: (OrderFilter) -> Unit,
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = filter.customer ?: "",
                onValueChange = { text ->
                    val value = text.ifBlank { null }
                    onFilterChange(filter.copy(customer = value))
                },
                label = { Text("Customer") },
                singleLine = true,
            )

            Spacer(Modifier.width(8.dp))

            Box {
                Button(onClick = { statusMenuExpanded = true }) {
                    val label = filter.status?.name ?: "All statuses"
                    Text(label)
                }
                DropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("All statuses") },
                        onClick = {
                            statusMenuExpanded = false
                            onFilterChange(filter.copy(status = null))
                        },
                    )
                    OrderStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                statusMenuExpanded = false
                                onFilterChange(filter.copy(status = status))
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = { onFilterChange(OrderFilter()) }) {
                Text("Clear")
            }
        }
    }
}