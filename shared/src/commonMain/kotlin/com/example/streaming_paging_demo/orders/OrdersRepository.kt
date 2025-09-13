package com.example.streaming_paging_demo.orders

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ua.wwind.paging.core.ExperimentalStreamingPagerApi
import ua.wwind.paging.core.PagingData
import ua.wwind.paging.core.StreamingPager
import ua.wwind.paging.core.StreamingPagerConfig

class OrdersRepository(
    private val dataSource: OrdersNetworkDataSource = OrdersNetworkDataSource(),
) {
    private val scope = CoroutineScope(SupervisorJob())

    private val logger = Logger.withTag("OrdersPager")

    @OptIn(ExperimentalStreamingPagerApi::class)
    fun getPagingOrders(filter: OrderFilter): Flow<PagingData<Order>> {
        val requestIdMutex = Mutex()
        var stagedRequestId: String? = null

        suspend fun getOrStageRequestId(): String = requestIdMutex.withLock {
            stagedRequestId ?: dataSource.stageSseFilters(filter).also { stagedRequestId = it }
        }

        return StreamingPager(
            scope = scope,
            readTotal = {
                flow {
                    var requestId = getOrStageRequestId()
                    try {
                        dataSource.totalCountFlow(requestId).collect {
                            emit(it.toInt())
                        }
                    } catch (e: ClientRequestException) {
                        if (e.response.status == HttpStatusCode.Forbidden) {
                            requestIdMutex.withLock { stagedRequestId = null }
                            requestId = getOrStageRequestId()
                            dataSource.totalCountFlow(requestId).collect {
                                emit(it.toInt())
                            }
                        } else throw e
                    }
                }
            },
            readPortion = { position, size ->
                flow {
                    var requestId = getOrStageRequestId()
                    try {
                        dataSource.ordersPortionFlow(position, size, requestId)
                            .collect { map ->
                                emit(map)
                            }
                    } catch (e: ClientRequestException) {
                        if (e.response.status == HttpStatusCode.Forbidden) {
                            logger.w { "readPortion 403 -> restage" }
                            requestIdMutex.withLock { stagedRequestId = null }
                            requestId = getOrStageRequestId()
                            dataSource.ordersPortionFlow(position, size, requestId)
                                .collect { map ->
                                    emit(map)
                                }
                        } else throw e
                    }
                }
            },
            config = StreamingPagerConfig(loadSize = 30, preloadSize = 60, cacheSize = 300),
        ).flow
    }
}
