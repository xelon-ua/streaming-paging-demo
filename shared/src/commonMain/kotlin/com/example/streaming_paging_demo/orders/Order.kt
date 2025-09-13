package com.example.streaming_paging_demo.orders

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Long,
    val orderDate: String, // ISO-8601 date (e.g., 2025-09-13)
    val customer: String,
    val deliveryAddress: String,
    val status: OrderStatus,
    val amount: Double,
)

@Serializable
data class OrderFilter(
    val orderDate: String? = null, // eq
    val customer: String? = null, // like
    val deliveryAddress: String? = null, // like
    val status: OrderStatus? = null, // eq
    val amount: Double? = null, // eq
)
