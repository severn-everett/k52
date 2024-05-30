package com.severett.k52.model

import com.severett.k52.util.toDateTimeString
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private const val GOOD_AMOUNT = 12345.678

class TransactionTest : FunSpec({
    test("A valid transaction serializes correctly") {
        val timestamp = Instant.now()
        val timestampStr = timestamp.toDateTimeString()
        val transaction = Json.decodeFromString<Transaction>(
            """{"amount":$GOOD_AMOUNT,"timestamp":"$timestampStr"}"""
        )
        assertSoftly {
            transaction.amount.toDouble() shouldBe GOOD_AMOUNT
            transaction.timestamp shouldBe timestamp
        }
    }

    test("Invalid JSON does not get parsed correctly") {
        @Suppress("JsonStandardCompliance")
        shouldThrow<SerializationException> { Json.decodeFromString<Transaction>("INVALID JSON") }
    }

    listOf("", "null", "\"NON_NUMBER\"").forEach { amountStr ->
        test("Parsing fails on invalid amount [$amountStr]") {
            val timestampStr = Instant.now().toDateTimeString()
            val rawStr = if (amountStr.isEmpty()) {
                """{"timestamp":"$timestampStr"}"""
            } else {
                """{"amount":$amountStr,"timestamp":"$timestampStr"}"""
            }
            shouldThrow<SerializationException> { Json.decodeFromString<Transaction>(rawStr) }
        }
    }

    listOf(
        "",
        "null",
        "5",
        "\"01/01/2021\"",
        "\"${Instant.now().plus(1, ChronoUnit.DAYS).toDateTimeString()}\"",
    ).forEach { timestampStr ->
        test("Parsing fails on invalid timestamp [$timestampStr]") {
            if (timestampStr.isEmpty()) {
                shouldThrow<SerializationException> {
                    Json.decodeFromString<Transaction>("""{"amount":$GOOD_AMOUNT}""")
                }
            } else {
                shouldThrow<DateTimeParseException> {
                    Json.decodeFromString<Transaction>("""{"amount":$GOOD_AMOUNT,"timestamp":"$timestampStr"}""")
                }
            }
        }
    }
})
