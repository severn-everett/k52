package com.severett.k52.model

import com.severett.k52.serde.BigDecimalSerializer
import com.severett.k52.serde.InstantSerializer
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant

@Serializable
@Resource("/transactions")
data class Transaction(
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant
) {
    init {
        val now = Instant.now()
        require(!timestamp.isAfter(now)) { "Timestamp '$timestamp' is after now ('$now')" }
    }
}
