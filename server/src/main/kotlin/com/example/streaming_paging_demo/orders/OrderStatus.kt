package com.example.streaming_paging_demo.orders

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    NEW,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    ON_HOLD,
    BACKORDER,
    REFUNDED,
    PARTIAL,
    PROCESSING,
}
