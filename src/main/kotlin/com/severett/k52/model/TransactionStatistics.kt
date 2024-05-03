package com.severett.k52.model

import com.severett.k52.serde.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class TransactionStatistics(
    @Serializable(with = BigDecimalSerializer::class)
    val sum: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val avg: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val min: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val max: BigDecimal,
    val count: Long,
)
