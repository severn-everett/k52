package com.severett.k52.services

import com.severett.k52.model.AddTransactionResult
import com.severett.k52.model.SecondBucket
import com.severett.k52.model.Transaction
import com.severett.k52.model.TransactionStatistics
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private const val SECONDS_WINDOW = 59L
private val logger = KotlinLogging.logger { }

class TransactionService {
    private val transactions = ConcurrentHashMap<Long, SecondBucket>()
    private val mutex = Mutex()

    suspend fun addTransaction(transaction: Transaction): AddTransactionResult {
        return if (transaction.timestamp.isBefore(Instant.now().minusSeconds(SECONDS_WINDOW))) {
            AddTransactionResult.TRANSACTION_EXPIRED
        } else {
            logger.debug { "Received transaction $transaction" }
            val secondBucket = transactions.computeIfAbsent(transaction.timestamp.epochSecond) { SecondBucket() }
            mutex.withLock {
                secondBucket.addTransaction(transaction)
            }
            AddTransactionResult.SUCCESS
        }
    }

    suspend fun getStatistics(): TransactionStatistics {
        val currentTimestamp = Instant.now()
        var sum = BigDecimal.ZERO
        var max: BigDecimal? = null
        var min: BigDecimal? = null
        var count = 0L
        for (second in currentTimestamp.minusSeconds(SECONDS_WINDOW).epochSecond..currentTimestamp.epochSecond) {
            transactions[second]?.let { secondBucket ->
                val secondStatistics = mutex.withLock { secondBucket.secondStatistics }
                sum += secondStatistics.sum
                secondStatistics.max?.let { secondMax ->
                    max = max?.let { currentMax ->
                        if (currentMax < secondMax) secondMax else currentMax
                    } ?: secondMax
                }
                secondStatistics.min?.let { secondMin ->
                    min = min?.let { currentMin ->
                        if (currentMin > secondMin) secondMin else currentMin
                    } ?: secondMin
                }
                count += secondStatistics.count
            }
        }
        val avg = if (count > 0L) sum / BigDecimal.valueOf(count) else BigDecimal.ZERO
        return TransactionStatistics(
            sum = sum,
            avg = avg,
            min = min ?: BigDecimal.ZERO,
            max = max?: BigDecimal.ZERO,
            count = count,
        )
    }

    fun deleteTransactions() {
        transactions.clear()
    }
}