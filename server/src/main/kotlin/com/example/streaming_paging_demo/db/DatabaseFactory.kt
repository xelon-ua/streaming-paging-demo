package com.example.streaming_paging_demo.db

import com.example.streaming_paging_demo.orders.OrderTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init() {
        // H2 in-memory DB. Keep alive until JVM stops.
        Database.connect(
            url = "jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED

        transaction {
            SchemaUtils.create(OrderTable)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction {
                block()
            }
        }
}
