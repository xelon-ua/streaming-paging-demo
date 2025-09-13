package com.example.streaming_paging_demo.orders

import com.example.streaming_paging_demo.db.DatabaseFactory.dbQuery
import com.example.streaming_paging_demo.db.DbChangeNotifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate
import kotlin.random.Random

@Serializable
data class OrderFilterDto(
    val orderDate: String? = null,
    val customer: String? = null,
    val deliveryAddress: String? = null,
    val status: OrderStatus? = null,
    val amount: Double? = null,
)

@Serializable
data class OrderRow(
    val id: Long,
    val orderDate: String,
    val customer: String,
    val deliveryAddress: String,
    val status: OrderStatus,
    val amount: Double,
)

class OrderRepository {
    private val customers = listOf(
        "Olivia Smith",
        "Liam Johnson",
        "Emma Williams",
        "Noah Brown",
        "Ava Jones",
        "Elijah Garcia",
        "Isabella Martinez",
        "Lucas Rodriguez",
        "Mia Davis",
        "Mason Hernandez",
        "Amelia Lopez",
        "Ethan Gonzalez",
    )

    private val addresses = List(10) { idx -> "City ${idx + 1}, Street ${idx + 1}" }
    private val statuses = OrderStatus.entries

    private fun rowToOrder(row: ResultRow): OrderRow =
        OrderRow(
            id = row[OrderTable.id],
            orderDate = row[OrderTable.orderDate],
            customer = row[OrderTable.customer],
            deliveryAddress = row[OrderTable.deliveryAddress],
            status = row[OrderTable.status],
            amount = row[OrderTable.amount],
        )

    suspend fun countAll(filter: OrderFilterDto?): Long = dbQuery {
        val query = applyFilter(filter)
        query.count()
    }

    suspend fun page(position: Int, size: Int, filter: OrderFilterDto?): List<OrderRow> = dbQuery {
        applyFilter(filter)
            .orderBy(OrderTable.id to SortOrder.ASC)
            .limit(size)
            .offset(position.toLong())
            .map(::rowToOrder)
    }

    private fun applyFilter(filter: OrderFilterDto?) =
        if (filter == null) OrderTable.selectAll() else OrderTable.selectAll().where { buildWhere(filter) }

    private fun buildWhere(filter: OrderFilterDto): Op<Boolean> = listOfNotNull(
        filter.orderDate?.let { OrderTable.orderDate eq it },
        filter.customer?.let { OrderTable.customer like "%$it%" },
        filter.deliveryAddress?.let { OrderTable.deliveryAddress like "%$it%" },
        filter.status?.let { OrderTable.status eq it },
        filter.amount?.let { OrderTable.amount eq it },
    ).reduceOrNull { acc, op -> acc and op } ?: Op.TRUE

    fun seedIfEmpty() {
        var insertedAny = false
        transaction {
            val cur = OrderTable.selectAll().count()
            if (cur > 0) return@transaction

            val today = LocalDate.now()

            // Build a single list of all combinations and insert from that list
            val combinations = customers.flatMap { customer ->
                addresses.flatMap { address ->
                    statuses.map { status -> Triple(customer, address, status) }
                }
            }.shuffled()
                .take(100)

            combinations.forEachIndexed { index, (customer, address, status) ->
                val id = index + 1L
                val dateOffsetDays = (id + 1L) % 30L
                val amountValue = ((id + 1L) % 1000L) + 10L

                OrderTable.insert { st ->
                    st[OrderTable.orderDate] = today.minusDays(dateOffsetDays).toString()
                    st[OrderTable.customer] = customer
                    st[OrderTable.deliveryAddress] = address
                    st[OrderTable.status] = status
                    st[OrderTable.amount] = amountValue.toDouble()
                }
            }
            insertedAny = true
        }
        if (insertedAny) {
            DbChangeNotifier.bump()
        }
    }

    suspend fun insertRandomOrder(): OrderRow {
        val result: OrderRow = dbQuery {
            val today = LocalDate.now()
            val randomDate = today.minusDays(Random.nextLong(from = 0, until = 30))
            val randomCustomer = customers.random()
            val randomAddress = addresses.random()
            val randomStatus = statuses.random()
            val randomAmount = Random.nextDouble(from = 10.0, until = 1010.0)

            val inserted = OrderTable.insert { st ->
                st[orderDate] = randomDate.toString()
                st[customer] = randomCustomer
                st[deliveryAddress] = randomAddress
                st[status] = randomStatus
                st[amount] = randomAmount
            }
            val id: Long = inserted.resultedValues?.single()?.get(OrderTable.id)
                ?: error("Failed to retrieve inserted id")

            // Fetch inserted row for return
            OrderTable
                .selectAll()
                .where { OrderTable.id eq id }
                .single()
                .let(::rowToOrder)
        }
        // Signal DB change after the transaction committed
        DbChangeNotifier.bump()
        return result
    }

    // Reactive flows that re-execute queries upon each DB change signal
    @OptIn(ExperimentalCoroutinesApi::class)
    fun countAllFlow(filter: OrderFilterDto?): Flow<Long> =
        DbChangeNotifier.version
            .mapLatest { countAll(filter) }
            .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun pageFlow(position: Int, size: Int, filter: OrderFilterDto?): Flow<List<OrderRow>> =
        DbChangeNotifier.version
            .mapLatest { page(position, size, filter) }
            .distinctUntilChanged()
}
