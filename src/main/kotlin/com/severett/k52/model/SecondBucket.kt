package com.severett.k52.model

import java.math.BigDecimal

private const val LESS_THAN = -1
private const val GREATER_THAN = 1

class SecondBucket {
    private var sum = BigDecimal.ZERO
    private var max: BigDecimal? = null
    private var min: BigDecimal? = null
    private var count = 0L

    val secondStatistics: SecondStatistics
        get() = SecondStatistics(sum = sum, max = max, min = min, count = count)

    fun addTransaction(transaction: Transaction) {
        val transactionAmt = transaction.amount
        max = assignAmt(max, transactionAmt, LESS_THAN)
        min = assignAmt(min, transactionAmt, GREATER_THAN)
        sum = sum.add(transactionAmt)
        count++
    }

    private fun assignAmt(
        initial: BigDecimal?,
        transactionAmt: BigDecimal,
        comparator: Int,
    ) = if (initial == null || initial.compareTo(transactionAmt) == comparator) {
        transactionAmt
    } else {
        initial
    }
}