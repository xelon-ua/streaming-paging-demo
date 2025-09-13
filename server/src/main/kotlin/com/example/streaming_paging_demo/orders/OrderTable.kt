package com.example.streaming_paging_demo.orders

import org.jetbrains.exposed.v1.core.Table

object OrderTable : Table("orders") {
    val id = long("id").autoIncrement()
    val orderDate = varchar("order_date", 16)
    val customer = varchar("customer", 128)
    val deliveryAddress = varchar("delivery_address", 256)
    val status = enumeration("status", OrderStatus::class)
    val amount = double("amount")

    override val primaryKey = PrimaryKey(id)
}
